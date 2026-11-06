package com.cloudforge.backend.service;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.dto.DeployRequest;
import com.cloudforge.backend.model.*;
import com.cloudforge.backend.repository.ApplicationRepository;
import com.cloudforge.backend.repository.ContainerRepository;
import com.cloudforge.backend.repository.EventRepository;
import com.cloudforge.backend.scheduler.WorkloadScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ContainerRepository containerRepository;
    private final EventRepository eventRepository;
    private final NodeService nodeService;
    private final WorkloadScheduler workloadScheduler;
    private final DockerService dockerService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ContainerRepository containerRepository,
            EventRepository eventRepository,
            NodeService nodeService,
            WorkloadScheduler workloadScheduler,
            DockerService dockerService
    ) {
        this.applicationRepository = applicationRepository;
        this.containerRepository = containerRepository;
        this.eventRepository = eventRepository;
        this.nodeService = nodeService;
        this.workloadScheduler = workloadScheduler;
        this.dockerService = dockerService;
    }

    public List<Application> getAllApplications() {
        List<Application> apps = applicationRepository.findAll();
        for (Application app : apps) {
            updateApplicationStatus(app);
        }
        return apps;
    }

    public Optional<Application> getApplicationById(String id) {
        Optional<Application> opt = applicationRepository.findById(id);
        opt.ifPresent(this::updateApplicationStatus);
        return opt;
    }

    public synchronized Application deployApplication(DeployRequest req) {
        String appId = "app-" + UUID.randomUUID().toString().substring(0, 8);
        Application app = new Application(
                appId,
                req.getName(),
                req.getImage(),
                req.getReplicas(),
                req.getRequestedCpu(),
                req.getRequestedMemoryMb()
        );
        app.setTargetPort(req.getTargetPort());
        app.setHostPort(req.getHostPort());
        app.setEnvVars(req.getEnvVars());
        app.setRestartPolicy(req.getRestartPolicy() != null ? req.getRestartPolicy() : "ALWAYS");
        app.setSchedulingStrategy(req.getSchedulingStrategy() != null ? req.getSchedulingStrategy() : "LEAST_ALLOCATED");
        app.setStatus("DEPLOYING");

        applicationRepository.save(app);

        DeploymentEvent startEvt = new DeploymentEvent("DEPLOY", "Deployment Started",
                String.format("Deploying %s (%d replicas, image: %s)", app.getName(), app.getReplicas(), app.getImage()), "INFO");
        startEvt.setApplicationId(app.getId());
        startEvt.setApplicationName(app.getName());
        eventRepository.save(startEvt);

        List<String> createdContainerIds = new ArrayList<>();
        boolean allScheduled = true;

        for (int i = 1; i <= app.getReplicas(); i++) {
            List<Node> currentNodes = nodeService.getAllNodes();
            SchedulingDecision decision = workloadScheduler.scheduleReplica(app, i, currentNodes, app.getSchedulingStrategy());

            if (!decision.isSuccess()) {
                allScheduled = false;
                DeploymentEvent failEvt = new DeploymentEvent("SCHEDULING_FAILED", "Workload Scheduling Failed",
                        String.format("Failed to place replica #%d of %s: %s", i, app.getName(), decision.getMessage()), "ERROR");
                failEvt.setApplicationId(app.getId());
                failEvt.setApplicationName(app.getName());
                eventRepository.save(failEvt);
                break;
            }

            String nodeId = decision.getSelectedNodeId();
            String nodeName = decision.getSelectedNodeName();
            nodeService.allocateResources(nodeId, app.getRequestedCpu(), app.getRequestedMemoryMb());

            // Create container instance
            String cntId = "cf-cnt-" + UUID.randomUUID().toString().substring(0, 8);
            String cntName = String.format("%s-rep-%d", app.getName().toLowerCase().replaceAll("[^a-z0-9-]", "-"), i);

            Integer hostPort = (app.getHostPort() != null) ? app.getHostPort() + (i - 1) : null;
            Integer targetPort = app.getTargetPort();

            ContainerInstance container = new ContainerInstance(
                    cntId,
                    cntName,
                    app.getId(),
                    app.getName(),
                    app.getImage(),
                    nodeId,
                    nodeName
            );
            container.setHostPort(hostPort);
            container.setTargetPort(targetPort);
            container.setMemoryLimitMb(app.getRequestedMemoryMb());

            try {
                String dockerContainerId = dockerService.createAndStartContainer(
                        app.getImage(),
                        cntName,
                        hostPort,
                        targetPort,
                        app.getEnvVars(),
                        app.getRequestedCpu(),
                        app.getRequestedMemoryMb()
                );
                container.setDockerContainerId(dockerContainerId);
                container.setStatus("RUNNING");
                container.setRealDocker(dockerService.isDockerAvailable() && !dockerContainerId.startsWith("sim-"));
                container.setStartedAt(Instant.now().toString());

                containerRepository.save(container);
                createdContainerIds.add(cntId);

                DeploymentEvent cntEvt = new DeploymentEvent("CONTAINER_START", "Container Placed & Started",
                        String.format("Replica #%d running on node %s (%s)", i, nodeName, cntName), "SUCCESS");
                cntEvt.setApplicationId(app.getId());
                cntEvt.setApplicationName(app.getName());
                cntEvt.setContainerId(cntId);
                cntEvt.setNodeId(nodeId);
                eventRepository.save(cntEvt);

            } catch (Exception e) {
                log.error("Failed to start container for app {}: {}", app.getName(), e.getMessage());
                container.setStatus("FAILED");
                container.setLastError(e.getMessage());
                containerRepository.save(container);
                createdContainerIds.add(cntId);
                allScheduled = false;
            }
        }

        app.setContainerIds(createdContainerIds);
        app.setStatus(allScheduled ? "RUNNING" : "DEGRADED");
        app.setUpdatedAt(Instant.now().toString());
        applicationRepository.save(app);

        DeploymentEvent finishEvt = new DeploymentEvent("DEPLOY_COMPLETED", "Deployment Complete",
                String.format("Application %s is %s with %d active container(s).", app.getName(), app.getStatus(), createdContainerIds.size()),
                allScheduled ? "SUCCESS" : "WARNING");
        finishEvt.setApplicationId(app.getId());
        finishEvt.setApplicationName(app.getName());
        eventRepository.save(finishEvt);

        return app;
    }

    public synchronized Application scaleApplication(String id, int targetReplicas) {
        Optional<Application> opt = applicationRepository.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Application not found: " + id);

        Application app = opt.get();
        int currentReplicas = app.getReplicas();
        if (targetReplicas == currentReplicas) return app;

        List<ContainerInstance> currentContainers = containerRepository.findByApplicationId(app.getId());

        if (targetReplicas > currentReplicas) {
            // Scale Up
            int toAdd = targetReplicas - currentReplicas;
            for (int i = 1; i <= toAdd; i++) {
                int repIdx = currentReplicas + i;
                List<Node> candidateNodes = nodeService.getAllNodes();
                SchedulingDecision decision = workloadScheduler.scheduleReplica(app, repIdx, candidateNodes, app.getSchedulingStrategy());

                if (decision.isSuccess()) {
                    String nodeId = decision.getSelectedNodeId();
                    String nodeName = decision.getSelectedNodeName();
                    nodeService.allocateResources(nodeId, app.getRequestedCpu(), app.getRequestedMemoryMb());

                    String cntId = "cf-cnt-" + UUID.randomUUID().toString().substring(0, 8);
                    String cntName = String.format("%s-rep-%d", app.getName().toLowerCase().replaceAll("[^a-z0-9-]", "-"), repIdx);
                    Integer hostPort = (app.getHostPort() != null) ? app.getHostPort() + (repIdx - 1) : null;

                    ContainerInstance cnt = new ContainerInstance(cntId, cntName, app.getId(), app.getName(), app.getImage(), nodeId, nodeName);
                    cnt.setHostPort(hostPort);
                    cnt.setTargetPort(app.getTargetPort());
                    cnt.setMemoryLimitMb(app.getRequestedMemoryMb());

                    try {
                        String dockerId = dockerService.createAndStartContainer(app.getImage(), cntName, hostPort, app.getTargetPort(), app.getEnvVars(), app.getRequestedCpu(), app.getRequestedMemoryMb());
                        cnt.setDockerContainerId(dockerId);
                        cnt.setStatus("RUNNING");
                        cnt.setStartedAt(Instant.now().toString());
                        cnt.setRealDocker(dockerService.isDockerAvailable() && !dockerId.startsWith("sim-"));
                        containerRepository.save(cnt);
                        app.getContainerIds().add(cntId);
                    } catch (Exception e) {
                        log.error("Failed to start replica during scale up: {}", e.getMessage());
                    }
                }
            }
        } else {
            // Scale Down
            int toRemove = currentReplicas - targetReplicas;
            List<ContainerInstance> runningList = new ArrayList<>(currentContainers);
            // Remove from the end
            for (int i = 0; i < toRemove && !runningList.isEmpty(); i++) {
                ContainerInstance toDelete = runningList.remove(runningList.size() - 1);
                try {
                    if (toDelete.getDockerContainerId() != null) {
                        dockerService.stopContainer(toDelete.getDockerContainerId());
                        dockerService.removeContainer(toDelete.getDockerContainerId());
                    }
                } catch (Exception ignored) {}

                nodeService.releaseResources(toDelete.getNodeId(), app.getRequestedCpu(), app.getRequestedMemoryMb());
                containerRepository.deleteById(toDelete.getId());
                app.getContainerIds().remove(toDelete.getId());
            }
        }

        app.setReplicas(targetReplicas);
        app.setUpdatedAt(Instant.now().toString());
        updateApplicationStatus(app);
        applicationRepository.save(app);

        DeploymentEvent scaleEvt = new DeploymentEvent("SCALE", "Application Scaled",
                String.format("Scaled %s from %d to %d replicas.", app.getName(), currentReplicas, targetReplicas), "INFO");
        scaleEvt.setApplicationId(app.getId());
        scaleEvt.setApplicationName(app.getName());
        eventRepository.save(scaleEvt);

        return app;
    }

    public synchronized void restartApplication(String id) {
        Optional<Application> opt = applicationRepository.findById(id);
        if (opt.isEmpty()) return;

        Application app = opt.get();
        List<ContainerInstance> containers = containerRepository.findByApplicationId(app.getId());
        for (ContainerInstance c : containers) {
            try {
                if (c.getDockerContainerId() != null) {
                    dockerService.restartContainer(c.getDockerContainerId());
                }
                c.setStatus("RUNNING");
                c.setStartedAt(Instant.now().toString());
                c.setRestartCount(c.getRestartCount() + 1);
                containerRepository.save(c);
            } catch (Exception e) {
                log.error("Failed to restart container {}: {}", c.getId(), e.getMessage());
            }
        }
        app.setStatus("RUNNING");
        app.setUpdatedAt(Instant.now().toString());
        applicationRepository.save(app);

        DeploymentEvent evt = new DeploymentEvent("APP_RESTART", "Rolling Restart Completed",
                String.format("Successfully restarted all %d replicas of %s.", containers.size(), app.getName()), "SUCCESS");
        evt.setApplicationId(app.getId());
        evt.setApplicationName(app.getName());
        eventRepository.save(evt);
    }

    public synchronized void stopApplication(String id) {
        Optional<Application> opt = applicationRepository.findById(id);
        if (opt.isEmpty()) return;

        Application app = opt.get();
        List<ContainerInstance> containers = containerRepository.findByApplicationId(app.getId());
        for (ContainerInstance c : containers) {
            try {
                if (c.getDockerContainerId() != null) {
                    dockerService.stopContainer(c.getDockerContainerId());
                }
                c.setStatus("STOPPED");
                containerRepository.save(c);
                nodeService.releaseResources(c.getNodeId(), app.getRequestedCpu(), app.getRequestedMemoryMb());
            } catch (Exception ignored) {}
        }
        app.setStatus("STOPPED");
        app.setUpdatedAt(Instant.now().toString());
        applicationRepository.save(app);

        DeploymentEvent evt = new DeploymentEvent("APP_STOP", "Application Stopped",
                String.format("Stopped application %s.", app.getName()), "INFO");
        evt.setApplicationId(app.getId());
        evt.setApplicationName(app.getName());
        eventRepository.save(evt);
    }

    public synchronized void deleteApplication(String id) {
        Optional<Application> opt = applicationRepository.findById(id);
        if (opt.isEmpty()) return;

        Application app = opt.get();
        List<ContainerInstance> containers = containerRepository.findByApplicationId(app.getId());
        for (ContainerInstance c : containers) {
            try {
                if (c.getDockerContainerId() != null) {
                    dockerService.stopContainer(c.getDockerContainerId());
                    dockerService.removeContainer(c.getDockerContainerId());
                }
            } catch (Exception ignored) {}
            nodeService.releaseResources(c.getNodeId(), app.getRequestedCpu(), app.getRequestedMemoryMb());
        }

        containerRepository.deleteAllByApplicationId(app.getId());
        applicationRepository.deleteById(app.getId());

        DeploymentEvent evt = new DeploymentEvent("APP_DELETE", "Application Deleted",
                String.format("Deleted application %s and released all cluster resources.", app.getName()), "WARNING");
        evt.setApplicationId(app.getId());
        evt.setApplicationName(app.getName());
        eventRepository.save(evt);
    }

    public synchronized void evacuateNode(String nodeId) {
        List<ContainerInstance> onNode = containerRepository.findByNodeId(nodeId);
        if (onNode.isEmpty()) return;

        log.info("Evacuating {} containers from node {}", onNode.size(), nodeId);
        for (ContainerInstance cnt : onNode) {
            applicationRepository.findById(cnt.getApplicationId()).ifPresent(app -> {
                // Stop container on current node
                try {
                    if (cnt.getDockerContainerId() != null) {
                        dockerService.stopContainer(cnt.getDockerContainerId());
                        dockerService.removeContainer(cnt.getDockerContainerId());
                    }
                } catch (Exception ignored) {}

                nodeService.releaseResources(nodeId, app.getRequestedCpu(), app.getRequestedMemoryMb());

                // Reschedule on a different ready node
                List<Node> healthyNodes = nodeService.getAllNodes().stream()
                        .filter(n -> !n.getId().equals(nodeId) && "READY".equalsIgnoreCase(n.getStatus()))
                        .toList();

                SchedulingDecision decision = workloadScheduler.scheduleReplica(app, 1, healthyNodes, app.getSchedulingStrategy());
                if (decision.isSuccess()) {
                    String targetNodeId = decision.getSelectedNodeId();
                    String targetNodeName = decision.getSelectedNodeName();
                    nodeService.allocateResources(targetNodeId, app.getRequestedCpu(), app.getRequestedMemoryMb());

                    try {
                        String newDockerId = dockerService.createAndStartContainer(
                                app.getImage(),
                                cnt.getName(),
                                cnt.getHostPort(),
                                cnt.getTargetPort(),
                                app.getEnvVars(),
                                app.getRequestedCpu(),
                                app.getRequestedMemoryMb()
                        );
                        cnt.setNodeId(targetNodeId);
                        cnt.setNodeName(targetNodeName);
                        cnt.setDockerContainerId(newDockerId);
                        cnt.setStatus("RUNNING");
                        cnt.setStartedAt(Instant.now().toString());
                        containerRepository.save(cnt);

                        DeploymentEvent evt = new DeploymentEvent("WORKLOAD_EVACUATION", "Workload Rescheduled",
                                String.format("Migrated container %s to %s during node evacuation.", cnt.getName(), targetNodeName), "SUCCESS");
                        evt.setApplicationId(app.getId());
                        evt.setContainerId(cnt.getId());
                        evt.setNodeId(targetNodeId);
                        eventRepository.save(evt);

                    } catch (Exception e) {
                        cnt.setStatus("FAILED");
                        cnt.setLastError("Failed to reschedule during evacuation: " + e.getMessage());
                        containerRepository.save(cnt);
                    }
                } else {
                    cnt.setStatus("PENDING");
                    cnt.setLastError("No eligible node available to evacuate container.");
                    containerRepository.save(cnt);
                }
            });
        }
    }

    private void updateApplicationStatus(Application app) {
        List<ContainerInstance> containers = containerRepository.findByApplicationId(app.getId());
        if (containers.isEmpty()) {
            if (!"STOPPED".equalsIgnoreCase(app.getStatus())) {
                app.setStatus("PENDING");
            }
            return;
        }

        long running = containers.stream().filter(c -> "RUNNING".equalsIgnoreCase(c.getStatus())).count();
        if (running == app.getReplicas()) {
            app.setStatus("RUNNING");
        } else if (running > 0) {
            app.setStatus("DEGRADED");
        } else {
            boolean allStopped = containers.stream().allMatch(c -> "STOPPED".equalsIgnoreCase(c.getStatus()));
            app.setStatus(allStopped ? "STOPPED" : "FAILED");
        }
    }
}

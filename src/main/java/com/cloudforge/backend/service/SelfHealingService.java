package com.cloudforge.backend.service;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.model.*;
import com.cloudforge.backend.repository.ApplicationRepository;
import com.cloudforge.backend.repository.ContainerRepository;
import com.cloudforge.backend.repository.EventRepository;
import com.cloudforge.backend.repository.NodeRepository;
import com.cloudforge.backend.scheduler.WorkloadScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SelfHealingService {

    private static final Logger log = LoggerFactory.getLogger(SelfHealingService.class);

    private final ContainerRepository containerRepository;
    private final ApplicationRepository applicationRepository;
    private final NodeRepository nodeRepository;
    private final EventRepository eventRepository;
    private final NodeService nodeService;
    private final WorkloadScheduler workloadScheduler;
    private final DockerService dockerService;

    @Value("${cloudforge.self-healing.enabled:true}")
    private boolean enabled;

    public SelfHealingService(
            ContainerRepository containerRepository,
            ApplicationRepository applicationRepository,
            NodeRepository nodeRepository,
            EventRepository eventRepository,
            NodeService nodeService,
            WorkloadScheduler workloadScheduler,
            DockerService dockerService
    ) {
        this.containerRepository = containerRepository;
        this.applicationRepository = applicationRepository;
        this.nodeRepository = nodeRepository;
        this.eventRepository = eventRepository;
        this.nodeService = nodeService;
        this.workloadScheduler = workloadScheduler;
        this.dockerService = dockerService;
    }

    @Scheduled(fixedDelayString = "${cloudforge.self-healing.interval-ms:5000}")
    public void runHealthAudit() {
        if (!enabled) return;

        List<ContainerInstance> containers = containerRepository.findAll();
        for (ContainerInstance container : containers) {
            if ("CRASHED".equalsIgnoreCase(container.getStatus())) {
                recoverCrashedContainer(container);
            }
        }
    }

    private synchronized void recoverCrashedContainer(ContainerInstance container) {
        Optional<Application> appOpt = applicationRepository.findById(container.getApplicationId());
        if (appOpt.isEmpty()) return;

        Application app = appOpt.get();
        if ("NEVER".equalsIgnoreCase(app.getRestartPolicy())) {
            return;
        }

        log.warn("Self-healing watchdog detected crashed container {} (App: {}). Initiating auto-recovery...",
                container.getName(), app.getName());

        Optional<Node> currentNodeOpt = nodeRepository.findById(container.getNodeId());
        boolean currentNodeHealthy = currentNodeOpt.isPresent() && "READY".equalsIgnoreCase(currentNodeOpt.get().getStatus());

        if (currentNodeHealthy) {
            // Restart on the same node
            try {
                if (container.getDockerContainerId() != null) {
                    dockerService.restartContainer(container.getDockerContainerId());
                }
                container.setStatus("RUNNING");
                container.setStartedAt(Instant.now().toString());
                container.setRestartCount(container.getRestartCount() + 1);
                container.setExitCode(null);
                container.setLastError(null);
                containerRepository.save(container);

                DeploymentEvent evt = new DeploymentEvent(
                        "SELF_HEALING",
                        "Automatic Self-Healing",
                        String.format("Auto-recovered crashed container %s on %s (Restart #%d).",
                                container.getName(), container.getNodeName(), container.getRestartCount()),
                        "SUCCESS"
                );
                evt.setApplicationId(app.getId());
                evt.setApplicationName(app.getName());
                evt.setContainerId(container.getId());
                evt.setNodeId(container.getNodeId());
                eventRepository.save(evt);

                log.info("Self-healing: Successfully revived container {} on node {}", container.getName(), container.getNodeName());
            } catch (Exception e) {
                log.error("Failed to self-heal container on same node: {}", e.getMessage());
            }
        } else {
            // Node is offline/drained -> failover container to another healthy node
            log.warn("Node {} is unhealthy. Triggering failover rescheduling for container {}", container.getNodeId(), container.getName());
            nodeService.releaseResources(container.getNodeId(), app.getRequestedCpu(), app.getRequestedMemoryMb());

            List<Node> healthyNodes = nodeRepository.findAll().stream()
                    .filter(n -> "READY".equalsIgnoreCase(n.getStatus()))
                    .toList();

            SchedulingDecision decision = workloadScheduler.scheduleReplica(app, 1, healthyNodes, app.getSchedulingStrategy());
            if (decision.isSuccess()) {
                String targetNodeId = decision.getSelectedNodeId();
                String targetNodeName = decision.getSelectedNodeName();
                nodeService.allocateResources(targetNodeId, app.getRequestedCpu(), app.getRequestedMemoryMb());

                try {
                    String newDockerId = dockerService.createAndStartContainer(
                            app.getImage(),
                            container.getName(),
                            container.getHostPort(),
                            container.getTargetPort(),
                            app.getEnvVars(),
                            app.getRequestedCpu(),
                            app.getRequestedMemoryMb()
                    );
                    container.setNodeId(targetNodeId);
                    container.setNodeName(targetNodeName);
                    container.setDockerContainerId(newDockerId);
                    container.setStatus("RUNNING");
                    container.setStartedAt(Instant.now().toString());
                    container.setRestartCount(container.getRestartCount() + 1);
                    container.setExitCode(null);
                    container.setLastError(null);
                    containerRepository.save(container);

                    DeploymentEvent evt = new DeploymentEvent(
                            "SELF_HEALING_FAILOVER",
                            "Self-Healing Failover",
                            String.format("Failover: Rescheduled crashed container %s onto healthy node %s.",
                                    container.getName(), targetNodeName),
                            "SUCCESS"
                    );
                    evt.setApplicationId(app.getId());
                    evt.setApplicationName(app.getName());
                    evt.setContainerId(container.getId());
                    evt.setNodeId(targetNodeId);
                    eventRepository.save(evt);

                    log.info("Self-healing failover: Successfully migrated container {} to {}", container.getName(), targetNodeName);
                } catch (Exception e) {
                    log.error("Failover container creation failed: {}", e.getMessage());
                }
            }
        }
    }
}

package com.cloudforge.backend.service;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.model.ContainerInstance;
import com.cloudforge.backend.model.DeploymentEvent;
import com.cloudforge.backend.repository.ContainerRepository;
import com.cloudforge.backend.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ContainerService {

    private static final Logger log = LoggerFactory.getLogger(ContainerService.class);

    private final ContainerRepository containerRepository;
    private final EventRepository eventRepository;
    private final DockerService dockerService;

    public ContainerService(
            ContainerRepository containerRepository,
            EventRepository eventRepository,
            DockerService dockerService
    ) {
        this.containerRepository = containerRepository;
        this.eventRepository = eventRepository;
        this.dockerService = dockerService;
    }

    public List<ContainerInstance> getAllContainers() {
        return containerRepository.findAll();
    }

    public Optional<ContainerInstance> getContainerById(String id) {
        return containerRepository.findById(id);
    }

    public List<ContainerInstance> getContainersByApplication(String appId) {
        return containerRepository.findByApplicationId(appId);
    }

    public List<ContainerInstance> getContainersByNode(String nodeId) {
        return containerRepository.findByNodeId(nodeId);
    }

    public synchronized boolean startContainer(String id) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return false;

        ContainerInstance c = opt.get();
        try {
            if (c.getDockerContainerId() != null) {
                dockerService.startContainer(c.getDockerContainerId());
            }
            c.setStatus("RUNNING");
            c.setStartedAt(Instant.now().toString());
            c.setExitCode(null);
            c.setLastError(null);
            containerRepository.save(c);

            DeploymentEvent evt = new DeploymentEvent("CONTAINER_START", "Container Started",
                    String.format("Container %s (%s) started on %s.", c.getName(), c.getId(), c.getNodeName()), "SUCCESS");
            evt.setApplicationId(c.getApplicationId());
            evt.setContainerId(c.getId());
            evt.setNodeId(c.getNodeId());
            eventRepository.save(evt);

            return true;
        } catch (Exception e) {
            log.error("Failed to start container {}: {}", id, e.getMessage());
            return false;
        }
    }

    public synchronized boolean stopContainer(String id) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return false;

        ContainerInstance c = opt.get();
        try {
            if (c.getDockerContainerId() != null) {
                dockerService.stopContainer(c.getDockerContainerId());
            }
            c.setStatus("STOPPED");
            c.setCpuPercent(0.0);
            c.setMemoryUsageMb(0);
            containerRepository.save(c);

            DeploymentEvent evt = new DeploymentEvent("CONTAINER_STOP", "Container Stopped",
                    String.format("Container %s (%s) stopped manually.", c.getName(), c.getId()), "INFO");
            evt.setApplicationId(c.getApplicationId());
            evt.setContainerId(c.getId());
            evt.setNodeId(c.getNodeId());
            eventRepository.save(evt);

            return true;
        } catch (Exception e) {
            log.error("Failed to stop container {}: {}", id, e.getMessage());
            return false;
        }
    }

    public synchronized boolean restartContainer(String id) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return false;

        ContainerInstance c = opt.get();
        try {
            if (c.getDockerContainerId() != null) {
                dockerService.restartContainer(c.getDockerContainerId());
            }
            c.setStatus("RUNNING");
            c.setStartedAt(Instant.now().toString());
            c.setRestartCount(c.getRestartCount() + 1);
            c.setExitCode(null);
            c.setLastError(null);
            containerRepository.save(c);

            DeploymentEvent evt = new DeploymentEvent("CONTAINER_RESTART", "Container Restarted",
                    String.format("Container %s (%s) restarted.", c.getName(), c.getId()), "SUCCESS");
            evt.setApplicationId(c.getApplicationId());
            evt.setContainerId(c.getId());
            evt.setNodeId(c.getNodeId());
            eventRepository.save(evt);

            return true;
        } catch (Exception e) {
            log.error("Failed to restart container {}: {}", id, e.getMessage());
            return false;
        }
    }

    public String getLogs(String id, int tailLines) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return "Container not found.";

        ContainerInstance c = opt.get();
        if (c.getDockerContainerId() != null) {
            return dockerService.getContainerLogs(c.getDockerContainerId(), tailLines);
        }
        return "No container runtime attached.";
    }

    public DockerService.ContainerStats getStats(String id) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return new DockerService.ContainerStats(0.0, 0, 0);

        ContainerInstance c = opt.get();
        if (c.getDockerContainerId() != null && "RUNNING".equalsIgnoreCase(c.getStatus())) {
            DockerService.ContainerStats stats = dockerService.getContainerStats(c.getDockerContainerId());
            c.setCpuPercent(stats.cpuPercent());
            c.setMemoryUsageMb(stats.memoryUsageMb());
            c.setMemoryLimitMb(stats.memoryLimitMb());
            return stats;
        }
        return new DockerService.ContainerStats(0.0, 0, 0);
    }

    public synchronized boolean simulateCrash(String id) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return false;

        ContainerInstance c = opt.get();
        c.setStatus("CRASHED");
        c.setExitCode(137); // OOM / SIGKILL
        c.setLastError("Fatal error: Process terminated by signal SIGKILL (Simulated Crash)");
        c.setCpuPercent(0.0);
        c.setMemoryUsageMb(0);
        containerRepository.save(c);

        DeploymentEvent evt = new DeploymentEvent("CONTAINER_CRASH", "Container Crashed (Simulated)",
                String.format("Container %s (%s) exited unexpectedly with code 137. Self-healing triggered.", c.getName(), c.getId()), "ERROR");
        evt.setApplicationId(c.getApplicationId());
        evt.setContainerId(c.getId());
        evt.setNodeId(c.getNodeId());
        eventRepository.save(evt);

        log.warn("Chaos test: Container {} crashed with exit code 137", id);
        return true;
    }

    public synchronized boolean deleteContainer(String id) {
        Optional<ContainerInstance> opt = containerRepository.findById(id);
        if (opt.isEmpty()) return false;

        ContainerInstance c = opt.get();
        try {
            if (c.getDockerContainerId() != null) {
                dockerService.removeContainer(c.getDockerContainerId());
            }
        } catch (Exception ignored) {}

        containerRepository.deleteById(id);
        return true;
    }
}

package com.cloudforge.backend.service;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.model.*;
import com.cloudforge.backend.repository.ApplicationRepository;
import com.cloudforge.backend.repository.ContainerRepository;
import com.cloudforge.backend.repository.NodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MetricsService {

    private static final int MAX_HISTORY = 30;

    private final NodeRepository nodeRepository;
    private final ApplicationRepository applicationRepository;
    private final ContainerRepository containerRepository;
    private final DockerService dockerService;

    private final List<ClusterMetrics> clusterHistory = new CopyOnWriteArrayList<>();
    private volatile ClusterMetrics currentMetrics = new ClusterMetrics();

    public MetricsService(
            NodeRepository nodeRepository,
            ApplicationRepository applicationRepository,
            ContainerRepository containerRepository,
            DockerService dockerService
    ) {
        this.nodeRepository = nodeRepository;
        this.applicationRepository = applicationRepository;
        this.containerRepository = containerRepository;
        this.dockerService = dockerService;
    }

    @PostConstruct
    public void init() {
        collectMetrics();
    }

    @Scheduled(fixedDelayString = "${cloudforge.monitoring.interval-ms:3000}")
    public void collectMetrics() {
        List<Node> nodes = nodeRepository.findAll();
        List<Application> apps = applicationRepository.findAll();
        List<ContainerInstance> containers = containerRepository.findAll();

        ClusterMetrics m = new ClusterMetrics();
        m.setTotalNodes(nodes.size());

        int healthy = 0, draining = 0, offline = 0;
        int totalCpu = 0;
        long totalMem = 0;
        double allocCpu = 0.0;
        long allocMem = 0;

        for (Node n : nodes) {
            if ("READY".equalsIgnoreCase(n.getStatus())) healthy++;
            else if ("DRAINING".equalsIgnoreCase(n.getStatus())) draining++;
            else offline++;

            totalCpu += n.getCpuCores();
            totalMem += n.getMemoryMb();
            allocCpu += n.getAllocatedCpuCores();
            allocMem += n.getAllocatedMemoryMb();
        }

        m.setHealthyNodes(healthy);
        m.setDrainingNodes(draining);
        m.setOfflineNodes(offline);
        m.setTotalCpuCores(totalCpu);
        m.setTotalMemoryMb(totalMem);
        m.setAllocatedCpuCores(allocCpu);
        m.setAllocatedMemoryMb(allocMem);

        double cpuUtil = totalCpu > 0 ? (allocCpu / totalCpu) * 100.0 : 0.0;
        double memUtil = totalMem > 0 ? ((double) allocMem / totalMem) * 100.0 : 0.0;
        m.setCpuUtilizationPercent(Math.round(cpuUtil * 10.0) / 10.0);
        m.setMemoryUtilizationPercent(Math.round(memUtil * 10.0) / 10.0);

        m.setTotalApplications(apps.size());
        m.setRunningApplications((int) apps.stream().filter(a -> "RUNNING".equalsIgnoreCase(a.getStatus())).count());

        m.setTotalContainers(containers.size());
        int runningCnt = 0, stoppedCnt = 0, crashedCnt = 0;
        for (ContainerInstance c : containers) {
            if ("RUNNING".equalsIgnoreCase(c.getStatus())) {
                runningCnt++;
                // Refresh container live stats
                if (c.getDockerContainerId() != null) {
                    DockerService.ContainerStats stats = dockerService.getContainerStats(c.getDockerContainerId());
                    c.setCpuPercent(stats.cpuPercent());
                    c.setMemoryUsageMb(stats.memoryUsageMb());
                    c.setMemoryLimitMb(stats.memoryLimitMb());
                    containerRepository.save(c);
                }
            } else if ("STOPPED".equalsIgnoreCase(c.getStatus())) {
                stoppedCnt++;
            } else if ("CRASHED".equalsIgnoreCase(c.getStatus())) {
                crashedCnt++;
            }
        }
        m.setRunningContainers(runningCnt);
        m.setStoppedContainers(stoppedCnt);
        m.setCrashedContainers(crashedCnt);

        m.setDockerConnected(dockerService.isDockerAvailable());

        this.currentMetrics = m;
        clusterHistory.add(m);
        if (clusterHistory.size() > MAX_HISTORY) {
            clusterHistory.remove(0);
        }
    }

    public ClusterMetrics getCurrentMetrics() {
        return currentMetrics;
    }

    public List<ClusterMetrics> getClusterHistory() {
        return new ArrayList<>(clusterHistory);
    }
}

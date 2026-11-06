package com.cloudforge.backend.controller;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final MetricsService metricsService;
    private final DockerService dockerService;

    public HealthController(MetricsService metricsService, DockerService dockerService) {
        this.metricsService = metricsService;
        this.dockerService = dockerService;
    }

    @GetMapping
    public Map<String, Object> health() {
        var metrics = metricsService.getCurrentMetrics();
        return Map.of(
                "status", "UP",
                "service", "CloudForge Orchestration Platform",
                "version", "1.0.0",
                "dockerConnected", dockerService.isDockerAvailable(),
                "healthyNodes", metrics.getHealthyNodes(),
                "runningContainers", metrics.getRunningContainers(),
                "activeApplications", metrics.getRunningApplications()
        );
    }
}

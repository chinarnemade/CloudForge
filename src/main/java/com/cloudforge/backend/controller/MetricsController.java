package com.cloudforge.backend.controller;

import com.cloudforge.backend.model.ClusterMetrics;
import com.cloudforge.backend.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/cluster")
    public ClusterMetrics getClusterMetrics() {
        return metricsService.getCurrentMetrics();
    }

    @GetMapping("/history")
    public List<ClusterMetrics> getClusterHistory() {
        return metricsService.getClusterHistory();
    }
}

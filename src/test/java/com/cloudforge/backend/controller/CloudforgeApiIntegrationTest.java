package com.cloudforge.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CloudforgeApiIntegrationTest {

    @Autowired
    private HealthController healthController;

    @Autowired
    private NodeController nodeController;

    @Autowired
    private DockerController dockerController;

    @Autowired
    private MetricsController metricsController;

    @Autowired
    private EventController eventController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                healthController,
                nodeController,
                dockerController,
                metricsController,
                eventController
        ).build();
    }

    @Test
    @DisplayName("GET /api/health returns UP status and cluster summary")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("CloudForge Orchestration Platform"));
    }

    @Test
    @DisplayName("GET /api/nodes returns cluster nodes")
    void testNodesEndpoint() throws Exception {
        mockMvc.perform(get("/api/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/docker/status returns Docker daemon state")
    void testDockerStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/docker/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").exists());
    }

    @Test
    @DisplayName("GET /api/metrics/cluster returns current cluster resource metrics")
    void testMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/api/metrics/cluster"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNodes").isNumber())
                .andExpect(jsonPath("$.totalCpuCores").isNumber());
    }

    @Test
    @DisplayName("GET /api/events returns event log")
    void testEventsEndpoint() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

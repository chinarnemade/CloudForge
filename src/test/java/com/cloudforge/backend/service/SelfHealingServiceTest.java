package com.cloudforge.backend.service;

import com.cloudforge.backend.dto.DeployRequest;
import com.cloudforge.backend.model.Application;
import com.cloudforge.backend.model.ContainerInstance;
import com.cloudforge.backend.repository.ContainerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SelfHealingServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private SelfHealingService selfHealingService;

    @Test
    @DisplayName("Self-healing watchdog detects crashed container and automatically recovers it")
    void testSelfHealingRecovery() {
        DeployRequest req = new DeployRequest();
        req.setName("auth-service");
        req.setImage("redis:alpine");
        req.setReplicas(1);
        req.setRequestedCpu(0.25);
        req.setRequestedMemoryMb(128);
        req.setRestartPolicy("ALWAYS");

        Application app = applicationService.deployApplication(req);
        List<ContainerInstance> containers = containerRepository.findByApplicationId(app.getId());
        assertEquals(1, containers.size());
        ContainerInstance container = containers.get(0);

        // Simulate crash
        containerService.simulateCrash(container.getId());

        ContainerInstance crashed = containerRepository.findById(container.getId()).orElseThrow();
        assertEquals("CRASHED", crashed.getStatus());

        // Run self-healing watchdog audit
        selfHealingService.runHealthAudit();

        ContainerInstance recovered = containerRepository.findById(container.getId()).orElseThrow();
        assertEquals("RUNNING", recovered.getStatus(), "Container should have been auto-healed to RUNNING");
        assertTrue(recovered.getRestartCount() >= 1, "Restart count should be incremented");

        // Clean up
        applicationService.deleteApplication(app.getId());
    }
}

package com.cloudforge.backend.service;

import com.cloudforge.backend.dto.DeployRequest;
import com.cloudforge.backend.model.Application;
import com.cloudforge.backend.model.ContainerInstance;
import com.cloudforge.backend.model.Node;
import com.cloudforge.backend.repository.ApplicationRepository;
import com.cloudforge.backend.repository.ContainerRepository;
import com.cloudforge.backend.repository.NodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Test
    @DisplayName("Deploying application creates containers and allocates node resources")
    void testDeployAndScaleApplication() {
        DeployRequest req = new DeployRequest();
        req.setName("payment-gateway");
        req.setImage("nginx:alpine");
        req.setReplicas(2);
        req.setRequestedCpu(0.5);
        req.setRequestedMemoryMb(256);
        req.setHostPort(9090);
        req.setTargetPort(80);
        req.setSchedulingStrategy("LEAST_ALLOCATED");

        Application app = applicationService.deployApplication(req);

        assertNotNull(app);
        assertNotNull(app.getId());
        assertEquals("RUNNING", app.getStatus());
        assertEquals(2, app.getContainerIds().size());

        List<ContainerInstance> containers = containerRepository.findByApplicationId(app.getId());
        assertEquals(2, containers.size());
        assertTrue(containers.stream().allMatch(c -> "RUNNING".equalsIgnoreCase(c.getStatus())));

        // Test scaling up to 3 replicas
        Application scaledUp = applicationService.scaleApplication(app.getId(), 3);
        assertEquals(3, scaledUp.getReplicas());
        assertEquals(3, containerRepository.findByApplicationId(app.getId()).size());

        // Test scaling down to 1 replica
        Application scaledDown = applicationService.scaleApplication(app.getId(), 1);
        assertEquals(1, scaledDown.getReplicas());
        assertEquals(1, containerRepository.findByApplicationId(app.getId()).size());

        // Clean up
        applicationService.deleteApplication(app.getId());
        assertFalse(applicationRepository.findById(app.getId()).isPresent());
        assertTrue(containerRepository.findByApplicationId(app.getId()).isEmpty());
    }
}

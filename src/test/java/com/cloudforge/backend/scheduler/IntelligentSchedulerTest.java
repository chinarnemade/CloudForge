package com.cloudforge.backend.scheduler;

import com.cloudforge.backend.model.Application;
import com.cloudforge.backend.model.Node;
import com.cloudforge.backend.model.SchedulingDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntelligentSchedulerTest {

    private IntelligentScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new IntelligentScheduler();
    }

    @Test
    @DisplayName("Least Allocated strategy should select the node with maximum remaining headroom")
    void testLeastAllocatedScheduling() {
        Node node1 = new Node("n1", "Node 1", "READY", 8, 16384);
        node1.setAllocatedCpuCores(6.0); // 2 cores free
        node1.setAllocatedMemoryMb(12288); // 4096 MB free

        Node node2 = new Node("n2", "Node 2", "READY", 8, 16384);
        node2.setAllocatedCpuCores(2.0); // 6 cores free
        node2.setAllocatedMemoryMb(4096); // 12288 MB free

        Application app = new Application("app-1", "web-api", "nginx:alpine", 1, 1.0, 1024);

        SchedulingDecision decision = scheduler.scheduleReplica(app, 1, List.of(node1, node2), "LEAST_ALLOCATED");

        assertTrue(decision.isSuccess());
        assertEquals("n2", decision.getSelectedNodeId(), "Should choose node2 because it has the most headroom");
    }

    @Test
    @DisplayName("Bin Packing strategy should select the eligible node with least remaining headroom")
    void testBinPackingScheduling() {
        Node node1 = new Node("n1", "Node 1", "READY", 8, 16384);
        node1.setAllocatedCpuCores(6.0); // 2 cores free (satisfies 1 core request)
        node1.setAllocatedMemoryMb(14000); // 2384 MB free (satisfies 1024 MB request)

        Node node2 = new Node("n2", "Node 2", "READY", 8, 16384);
        node2.setAllocatedCpuCores(1.0); // 7 cores free
        node2.setAllocatedMemoryMb(2048); // 14336 MB free

        Application app = new Application("app-1", "web-api", "nginx:alpine", 1, 1.0, 1024);

        SchedulingDecision decision = scheduler.scheduleReplica(app, 1, List.of(node1, node2), "BIN_PACKING");

        assertTrue(decision.isSuccess());
        assertEquals("n1", decision.getSelectedNodeId(), "Bin packing should select node1 to pack densely");
    }

    @Test
    @DisplayName("Scheduler should reject offline or draining nodes")
    void testSchedulerRejectsNonReadyNodes() {
        Node drainingNode = new Node("n1", "Node Draining", "DRAINING", 8, 16384);
        Node offlineNode = new Node("n2", "Node Offline", "OFFLINE", 8, 16384);

        Application app = new Application("app-1", "web-api", "nginx:alpine", 1, 1.0, 1024);

        SchedulingDecision decision = scheduler.scheduleReplica(app, 1, List.of(drainingNode, offlineNode), "LEAST_ALLOCATED");

        assertFalse(decision.isSuccess());
        assertNull(decision.getSelectedNodeId());
        assertTrue(decision.getMessage().contains("No candidate node satisfies"));
    }

    @Test
    @DisplayName("Scheduler should reject node when memory or CPU request exceeds capacity")
    void testInsufficientResourceRejection() {
        Node smallNode = new Node("n1", "Small Node", "READY", 2, 2048);
        smallNode.setAllocatedCpuCores(1.5); // 0.5 cores free
        smallNode.setAllocatedMemoryMb(1536); // 512 MB free

        Application app = new Application("app-1", "heavy-app", "redis:alpine", 1, 1.0, 1024);

        SchedulingDecision decision = scheduler.scheduleReplica(app, 1, List.of(smallNode), "LEAST_ALLOCATED");

        assertFalse(decision.isSuccess());
        assertEquals(1, decision.getCandidateEvaluations().size());
        assertFalse(decision.getCandidateEvaluations().get(0).isEligible());
    }
}

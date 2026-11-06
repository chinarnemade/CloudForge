package com.cloudforge.backend.service;

import com.cloudforge.backend.dto.NodeCreateRequest;
import com.cloudforge.backend.model.DeploymentEvent;
import com.cloudforge.backend.model.Node;
import com.cloudforge.backend.repository.ContainerRepository;
import com.cloudforge.backend.repository.EventRepository;
import com.cloudforge.backend.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeService {

    private static final Logger log = LoggerFactory.getLogger(NodeService.class);

    private final NodeRepository nodeRepository;
    private final ContainerRepository containerRepository;
    private final EventRepository eventRepository;
    private final ApplicationService applicationService;

    public NodeService(
            NodeRepository nodeRepository,
            ContainerRepository containerRepository,
            EventRepository eventRepository,
            @Lazy ApplicationService applicationService
    ) {
        this.nodeRepository = nodeRepository;
        this.containerRepository = containerRepository;
        this.eventRepository = eventRepository;
        this.applicationService = applicationService;
    }

    public List<Node> getAllNodes() {
        List<Node> nodes = nodeRepository.findAll();
        // Update live container counts
        for (Node node : nodes) {
            int running = (int) containerRepository.findByNodeId(node.getId()).stream()
                    .filter(c -> "RUNNING".equalsIgnoreCase(c.getStatus()))
                    .count();
            node.setRunningContainers(running);
        }
        return nodes;
    }

    public Optional<Node> getNodeById(String id) {
        return nodeRepository.findById(id);
    }

    public Node registerNode(NodeCreateRequest request) {
        String id = "node-" + UUID.randomUUID().toString().substring(0, 8);
        Node node = new Node(id, request.getName(), "READY", request.getCpuCores(), request.getMemoryMb());
        node.setIpAddress(request.getIpAddress() != null ? request.getIpAddress() : "192.168.1." + (new Random().nextInt(150) + 100));
        if (request.getLabels() != null) {
            node.getLabels().putAll(request.getLabels());
        }
        node.getLabels().putIfAbsent("tier", "worker");
        node.getLabels().putIfAbsent("env", "production");

        nodeRepository.save(node);

        DeploymentEvent evt = new DeploymentEvent("NODE_REGISTER", "Node Registered",
                String.format("Added node %s (%d cores, %d MB RAM) to cluster.", node.getName(), node.getCpuCores(), node.getMemoryMb()), "SUCCESS");
        evt.setNodeId(node.getId());
        eventRepository.save(evt);

        log.info("Registered new cluster node: {} ({})", node.getName(), node.getId());
        return node;
    }

    public synchronized boolean drainNode(String id) {
        Optional<Node> opt = nodeRepository.findById(id);
        if (opt.isEmpty()) return false;

        Node node = opt.get();
        node.setStatus("DRAINING");
        nodeRepository.save(node);

        DeploymentEvent evt = new DeploymentEvent("NODE_DRAIN", "Node Cordoned / Drained",
                String.format("Node %s marked as DRAINING. Migrating active workloads...", node.getName()), "WARNING");
        evt.setNodeId(node.getId());
        eventRepository.save(evt);

        // Migrate containers running on this node to other ready nodes
        applicationService.evacuateNode(id);

        log.info("Drained node {}", id);
        return true;
    }

    public synchronized boolean uncordonNode(String id) {
        Optional<Node> opt = nodeRepository.findById(id);
        if (opt.isEmpty()) return false;

        Node node = opt.get();
        node.setStatus("READY");
        nodeRepository.save(node);

        DeploymentEvent evt = new DeploymentEvent("NODE_READY", "Node Uncordoned",
                String.format("Node %s returned to READY status.", node.getName()), "SUCCESS");
        evt.setNodeId(node.getId());
        eventRepository.save(evt);

        log.info("Uncordoned node {}", id);
        return true;
    }

    public synchronized boolean simulateFailure(String id, boolean offline) {
        Optional<Node> opt = nodeRepository.findById(id);
        if (opt.isEmpty()) return false;

        Node node = opt.get();
        String newStatus = offline ? "OFFLINE" : "READY";
        node.setStatus(newStatus);
        nodeRepository.save(node);

        String severity = offline ? "ERROR" : "SUCCESS";
        String title = offline ? "Node Outage Simulated" : "Node Recovered";
        String msg = String.format("Node %s status changed to %s. Fault management activated.", node.getName(), newStatus);

        DeploymentEvent evt = new DeploymentEvent("NODE_STATUS_CHANGE", title, msg, severity);
        evt.setNodeId(node.getId());
        eventRepository.save(evt);

        if (offline) {
            applicationService.evacuateNode(id);
        }

        return true;
    }

    public synchronized void allocateResources(String nodeId, double cpuCores, long memoryMb) {
        nodeRepository.findById(nodeId).ifPresent(node -> {
            node.setAllocatedCpuCores(node.getAllocatedCpuCores() + cpuCores);
            node.setAllocatedMemoryMb(node.getAllocatedMemoryMb() + memoryMb);
            nodeRepository.save(node);
        });
    }

    public synchronized void releaseResources(String nodeId, double cpuCores, long memoryMb) {
        nodeRepository.findById(nodeId).ifPresent(node -> {
            node.setAllocatedCpuCores(Math.max(0.0, node.getAllocatedCpuCores() - cpuCores));
            node.setAllocatedMemoryMb(Math.max(0L, node.getAllocatedMemoryMb() - memoryMb));
            nodeRepository.save(node);
        });
    }

    public synchronized boolean deleteNode(String id) {
        return nodeRepository.deleteById(id);
    }

    public void resetCluster() {
        nodeRepository.initDefaultNodes();
    }
}

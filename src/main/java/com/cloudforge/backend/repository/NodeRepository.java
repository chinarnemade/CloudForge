package com.cloudforge.backend.repository;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.model.DockerStatus;
import com.cloudforge.backend.model.Node;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NodeRepository {

    private final PersistenceService persistenceService;
    private final DockerService dockerService;
    private final Map<String, Node> nodeMap = new ConcurrentHashMap<>();

    public NodeRepository(PersistenceService persistenceService, DockerService dockerService) {
        this.persistenceService = persistenceService;
        this.dockerService = dockerService;
    }

    @PostConstruct
    public void init() {
        List<Node> loaded = persistenceService.loadList("nodes.json", new TypeReference<List<Node>>() {});
        if (!loaded.isEmpty()) {
            loaded.forEach(n -> nodeMap.put(n.getId(), n));
        } else {
            initDefaultNodes();
        }
    }

    public synchronized void initDefaultNodes() {
        nodeMap.clear();

        // 1. Local Node (Controller / Docker host)
        DockerStatus dockerStatus = dockerService.checkStatus();
        int localCpus = dockerStatus.getTotalCpus() > 0 ? dockerStatus.getTotalCpus() : Runtime.getRuntime().availableProcessors();
        long localMemMb = dockerStatus.getTotalMemoryBytes() > 0 ? dockerStatus.getTotalMemoryBytes() / (1024 * 1024) : 16384;

        Node localNode = new Node("node-local", "CloudForge Host (Local Node)", "READY", localCpus, localMemMb);
        localNode.setLocalHost(true);
        localNode.setIpAddress("127.0.0.1");
        localNode.setDockerHostUri(dockerStatus.getDockerHost());
        localNode.getLabels().put("tier", "control-plane");
        localNode.getLabels().put("env", "development");
        localNode.getLabels().put("zone", "local-dc");
        nodeMap.put(localNode.getId(), localNode);

        // 2. Worker Node 1
        Node worker1 = new Node("node-worker-1", "Worker Node 01 (Compute)", "READY", 8, 16384);
        worker1.setIpAddress("192.168.1.101");
        worker1.getLabels().put("tier", "worker");
        worker1.getLabels().put("env", "production");
        worker1.getLabels().put("zone", "us-east-1a");
        nodeMap.put(worker1.getId(), worker1);

        // 3. Worker Node 2
        Node worker2 = new Node("node-worker-2", "Worker Node 02 (High-Mem)", "READY", 4, 32768);
        worker2.setIpAddress("192.168.1.102");
        worker2.getLabels().put("tier", "worker");
        worker2.getLabels().put("env", "production");
        worker2.getLabels().put("zone", "us-east-1b");
        nodeMap.put(worker2.getId(), worker2);

        persist();
    }

    public synchronized Node save(Node node) {
        nodeMap.put(node.getId(), node);
        persist();
        return node;
    }

    public Optional<Node> findById(String id) {
        return Optional.ofNullable(nodeMap.get(id));
    }

    public List<Node> findAll() {
        return new ArrayList<>(nodeMap.values());
    }

    public synchronized boolean deleteById(String id) {
        Node removed = nodeMap.remove(id);
        if (removed != null) {
            persist();
            return true;
        }
        return false;
    }

    public boolean existsById(String id) {
        return nodeMap.containsKey(id);
    }

    private synchronized void persist() {
        persistenceService.saveList("nodes.json", new ArrayList<>(nodeMap.values()));
    }
}

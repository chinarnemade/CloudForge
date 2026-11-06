package com.cloudforge.backend.controller;

import com.cloudforge.backend.dto.NodeCreateRequest;
import com.cloudforge.backend.model.Node;
import com.cloudforge.backend.service.NodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final NodeService nodeService;

    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping
    public List<Node> getAllNodes() {
        return nodeService.getAllNodes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Node> getNodeById(@PathVariable String id) {
        return nodeService.getNodeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Node> registerNode(@RequestBody NodeCreateRequest request) {
        Node node = nodeService.registerNode(request);
        return ResponseEntity.ok(node);
    }

    @PostMapping("/{id}/drain")
    public ResponseEntity<Map<String, Object>> drainNode(@PathVariable String id) {
        boolean success = nodeService.drainNode(id);
        return ResponseEntity.ok(Map.of(
                "nodeId", id,
                "success", success,
                "message", success ? "Node drained and workloads evacuated" : "Node not found"
        ));
    }

    @PostMapping("/{id}/uncordon")
    public ResponseEntity<Map<String, Object>> uncordonNode(@PathVariable String id) {
        boolean success = nodeService.uncordonNode(id);
        return ResponseEntity.ok(Map.of(
                "nodeId", id,
                "success", success,
                "message", success ? "Node uncordoned and returned to READY" : "Node not found"
        ));
    }

    @PostMapping("/{id}/simulate-failure")
    public ResponseEntity<Map<String, Object>> simulateFailure(
            @PathVariable String id,
            @RequestParam(defaultValue = "true") boolean offline
    ) {
        boolean success = nodeService.simulateFailure(id, offline);
        return ResponseEntity.ok(Map.of(
                "nodeId", id,
                "offline", offline,
                "success", success,
                "message", success ? "Node outage state updated" : "Node not found"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNode(@PathVariable String id) {
        boolean success = nodeService.deleteNode(id);
        return ResponseEntity.ok(Map.of(
                "nodeId", id,
                "success", success
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetCluster() {
        nodeService.resetCluster();
        return ResponseEntity.ok(Map.of("message", "Cluster topology reset to initial state"));
    }
}

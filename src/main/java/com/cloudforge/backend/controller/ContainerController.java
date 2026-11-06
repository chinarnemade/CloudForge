package com.cloudforge.backend.controller;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.model.ContainerInstance;
import com.cloudforge.backend.service.ContainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/containers")
public class ContainerController {

    private final ContainerService containerService;

    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public List<ContainerInstance> getAllContainers(
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String nodeId
    ) {
        if (applicationId != null && !applicationId.trim().isEmpty()) {
            return containerService.getContainersByApplication(applicationId);
        }
        if (nodeId != null && !nodeId.trim().isEmpty()) {
            return containerService.getContainersByNode(nodeId);
        }
        return containerService.getAllContainers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContainerInstance> getContainerById(@PathVariable String id) {
        return containerService.getContainerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startContainer(@PathVariable String id) {
        boolean success = containerService.startContainer(id);
        return ResponseEntity.ok(Map.of(
                "containerId", id,
                "success", success
        ));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopContainer(@PathVariable String id) {
        boolean success = containerService.stopContainer(id);
        return ResponseEntity.ok(Map.of(
                "containerId", id,
                "success", success
        ));
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<Map<String, Object>> restartContainer(@PathVariable String id) {
        boolean success = containerService.restartContainer(id);
        return ResponseEntity.ok(Map.of(
                "containerId", id,
                "success", success
        ));
    }

    @PostMapping("/{id}/crash")
    public ResponseEntity<Map<String, Object>> simulateCrash(@PathVariable String id) {
        boolean success = containerService.simulateCrash(id);
        return ResponseEntity.ok(Map.of(
                "containerId", id,
                "success", success,
                "message", "Simulated fatal crash (SIGKILL). Watch self-healing automatically restore this container!"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContainer(@PathVariable String id) {
        boolean success = containerService.deleteContainer(id);
        return ResponseEntity.ok(Map.of(
                "containerId", id,
                "success", success
        ));
    }

    @GetMapping("/{id}/logs")
    public Map<String, Object> getLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "100") int tail
    ) {
        String logs = containerService.getLogs(id, tail);
        return Map.of(
                "containerId", id,
                "logs", logs
        );
    }

    @GetMapping("/{id}/stats")
    public DockerService.ContainerStats getStats(@PathVariable String id) {
        return containerService.getStats(id);
    }
}

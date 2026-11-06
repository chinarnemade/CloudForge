package com.cloudforge.backend.controller;

import com.cloudforge.backend.docker.DockerService;
import com.cloudforge.backend.model.DockerStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/docker")
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/status")
    public DockerStatus getStatus() {
        return dockerService.checkStatus();
    }

    @PostMapping("/reconnect")
    public DockerStatus reconnect(@RequestParam(required = false) String host) {
        return dockerService.reconnect(host);
    }

    @PostMapping("/pull")
    public ResponseEntity<Map<String, Object>> pullImage(@RequestParam String image) {
        boolean success = dockerService.pullImage(image);
        return ResponseEntity.ok(Map.of(
                "image", image,
                "success", success,
                "message", success ? "Image pulled successfully" : "Failed to pull image or image not found"
        ));
    }
}

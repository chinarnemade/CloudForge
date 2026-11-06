package com.cloudforge.backend.controller;

import com.cloudforge.backend.dto.DeployRequest;
import com.cloudforge.backend.dto.ScaleRequest;
import com.cloudforge.backend.model.Application;
import com.cloudforge.backend.model.ContainerInstance;
import com.cloudforge.backend.service.ApplicationService;
import com.cloudforge.backend.service.ContainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ContainerService containerService;

    public ApplicationController(ApplicationService applicationService, ContainerService containerService) {
        this.applicationService = applicationService;
        this.containerService = containerService;
    }

    @GetMapping
    public List<Application> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationById(@PathVariable String id) {
        return applicationService.getApplicationById(id)
                .map(app -> {
                    List<ContainerInstance> containers = containerService.getContainersByApplication(id);
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("application", app);
                    resp.put("containers", containers);
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Application> deployApplication(@RequestBody DeployRequest request) {
        Application app = applicationService.deployApplication(request);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{id}/scale")
    public ResponseEntity<Application> scaleApplication(
            @PathVariable String id,
            @RequestBody ScaleRequest request
    ) {
        Application app = applicationService.scaleApplication(id, request.getReplicas());
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<Map<String, Object>> restartApplication(@PathVariable String id) {
        applicationService.restartApplication(id);
        return ResponseEntity.ok(Map.of(
                "applicationId", id,
                "message", "Rolling restart triggered"
        ));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopApplication(@PathVariable String id) {
        applicationService.stopApplication(id);
        return ResponseEntity.ok(Map.of(
                "applicationId", id,
                "message", "Application stopped"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok(Map.of(
                "applicationId", id,
                "message", "Application deleted"
        ));
    }
}

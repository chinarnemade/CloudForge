package com.cloudforge.backend.controller;

import com.cloudforge.backend.model.DeploymentEvent;
import com.cloudforge.backend.repository.EventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public List<DeploymentEvent> getEvents(@RequestParam(defaultValue = "50") int limit) {
        return eventRepository.findAll(limit);
    }

    @GetMapping("/applications/{appId}")
    public List<DeploymentEvent> getEventsByApplication(
            @PathVariable String appId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return eventRepository.findByApplicationId(appId, limit);
    }

    @GetMapping("/nodes/{nodeId}")
    public List<DeploymentEvent> getEventsByNode(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return eventRepository.findByNodeId(nodeId, limit);
    }
}

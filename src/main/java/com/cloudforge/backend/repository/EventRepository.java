package com.cloudforge.backend.repository;

import com.cloudforge.backend.model.DeploymentEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class EventRepository {

    private static final int MAX_EVENTS = 500;
    private final PersistenceService persistenceService;
    private final List<DeploymentEvent> events = new CopyOnWriteArrayList<>();

    public EventRepository(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void init() {
        List<DeploymentEvent> list = persistenceService.loadList("events.json", new TypeReference<List<DeploymentEvent>>() {});
        events.addAll(list);
    }

    public synchronized DeploymentEvent save(DeploymentEvent event) {
        events.add(0, event); // newest first
        if (events.size() > MAX_EVENTS) {
            events.remove(events.size() - 1);
        }
        persist();
        return event;
    }

    public List<DeploymentEvent> findAll(int limit) {
        int cap = Math.min(limit, events.size());
        return new ArrayList<>(events.subList(0, cap));
    }

    public List<DeploymentEvent> findByApplicationId(String applicationId, int limit) {
        return events.stream()
                .filter(e -> applicationId.equals(e.getApplicationId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<DeploymentEvent> findByNodeId(String nodeId, int limit) {
        return events.stream()
                .filter(e -> nodeId.equals(e.getNodeId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private synchronized void persist() {
        persistenceService.saveList("events.json", new ArrayList<>(events));
    }
}

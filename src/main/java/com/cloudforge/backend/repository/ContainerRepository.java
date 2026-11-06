package com.cloudforge.backend.repository;

import com.cloudforge.backend.model.ContainerInstance;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ContainerRepository {

    private final PersistenceService persistenceService;
    private final Map<String, ContainerInstance> containerMap = new ConcurrentHashMap<>();

    public ContainerRepository(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void init() {
        List<ContainerInstance> list = persistenceService.loadList("containers.json", new TypeReference<List<ContainerInstance>>() {});
        list.forEach(c -> containerMap.put(c.getId(), c));
    }

    public synchronized ContainerInstance save(ContainerInstance container) {
        containerMap.put(container.getId(), container);
        persist();
        return container;
    }

    public Optional<ContainerInstance> findById(String id) {
        return Optional.ofNullable(containerMap.get(id));
    }

    public Optional<ContainerInstance> findByDockerContainerId(String dockerId) {
        return containerMap.values().stream()
                .filter(c -> dockerId.equals(c.getDockerContainerId()))
                .findFirst();
    }

    public List<ContainerInstance> findAll() {
        return new ArrayList<>(containerMap.values());
    }

    public List<ContainerInstance> findByApplicationId(String applicationId) {
        return containerMap.values().stream()
                .filter(c -> applicationId.equals(c.getApplicationId()))
                .collect(Collectors.toList());
    }

    public List<ContainerInstance> findByNodeId(String nodeId) {
        return containerMap.values().stream()
                .filter(c -> nodeId.equals(c.getNodeId()))
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteById(String id) {
        ContainerInstance removed = containerMap.remove(id);
        if (removed != null) {
            persist();
            return true;
        }
        return false;
    }

    public synchronized void deleteAllByApplicationId(String applicationId) {
        containerMap.values().removeIf(c -> applicationId.equals(c.getApplicationId()));
        persist();
    }

    private synchronized void persist() {
        persistenceService.saveList("containers.json", new ArrayList<>(containerMap.values()));
    }
}

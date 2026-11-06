package com.cloudforge.backend.repository;

import com.cloudforge.backend.model.Application;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ApplicationRepository {

    private final PersistenceService persistenceService;
    private final Map<String, Application> appMap = new ConcurrentHashMap<>();

    public ApplicationRepository(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void init() {
        List<Application> list = persistenceService.loadList("applications.json", new TypeReference<List<Application>>() {});
        list.forEach(a -> appMap.put(a.getId(), a));
    }

    public synchronized Application save(Application app) {
        appMap.put(app.getId(), app);
        persist();
        return app;
    }

    public Optional<Application> findById(String id) {
        return Optional.ofNullable(appMap.get(id));
    }

    public Optional<Application> findByName(String name) {
        return appMap.values().stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Application> findAll() {
        return new ArrayList<>(appMap.values());
    }

    public synchronized boolean deleteById(String id) {
        Application removed = appMap.remove(id);
        if (removed != null) {
            persist();
            return true;
        }
        return false;
    }

    private synchronized void persist() {
        persistenceService.saveList("applications.json", new ArrayList<>(appMap.values()));
    }
}

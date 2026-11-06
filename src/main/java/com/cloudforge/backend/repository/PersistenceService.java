package com.cloudforge.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

@Service
public class PersistenceService {

    private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);

    @Value("${cloudforge.storage.data-dir:./data}")
    private String dataDir;

    private final ObjectMapper objectMapper;

    public PersistenceService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            log.info("Initialized persistence data directory at {}: {}", dir.getAbsolutePath(), created);
        }
    }

    public synchronized <T> void saveList(String filename, List<T> items) {
        try {
            File dir = new File(dataDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File targetFile = new File(dir, filename);
            File tmpFile = new File(dir, filename + ".tmp");

            objectMapper.writeValue(tmpFile, items);
            Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.error("Failed to persist {} to disk: {}", filename, e.getMessage());
        }
    }

    public synchronized <T> List<T> loadList(String filename, TypeReference<List<T>> typeRef) {
        File file = new File(dataDir, filename);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(file, typeRef);
        } catch (IOException e) {
            log.warn("Could not load {} from disk ({}), starting with empty state.", filename, e.getMessage());
            return Collections.emptyList();
        }
    }
}

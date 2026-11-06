package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Application {

    private String id;
    private String name;
    private String image;
    private int replicas;
    private double requestedCpu;
    private long requestedMemoryMb;
    private Integer targetPort;
    private Integer hostPort;
    private Map<String, String> envVars = new HashMap<>();
    private String restartPolicy; // ALWAYS, ON_FAILURE, NEVER
    private String schedulingStrategy; // LEAST_ALLOCATED, BIN_PACKING, ROUND_ROBIN
    private String status; // PENDING, DEPLOYING, RUNNING, DEGRADED, STOPPED, FAILED
    private String createdAt;
    private String updatedAt;
    private List<String> containerIds = new ArrayList<>();

    public Application() {
        this.createdAt = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
        this.replicas = 1;
        this.requestedCpu = 0.5;
        this.requestedMemoryMb = 256;
        this.restartPolicy = "ALWAYS";
        this.schedulingStrategy = "LEAST_ALLOCATED";
        this.status = "PENDING";
    }

    public Application(String id, String name, String image, int replicas, double requestedCpu, long requestedMemoryMb) {
        this();
        this.id = id;
        this.name = name;
        this.image = image;
        this.replicas = replicas;
        this.requestedCpu = requestedCpu;
        this.requestedMemoryMb = requestedMemoryMb;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public double getRequestedCpu() {
        return requestedCpu;
    }

    public void setRequestedCpu(double requestedCpu) {
        this.requestedCpu = requestedCpu;
    }

    public long getRequestedMemoryMb() {
        return requestedMemoryMb;
    }

    public void setRequestedMemoryMb(long requestedMemoryMb) {
        this.requestedMemoryMb = requestedMemoryMb;
    }

    public Integer getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(Integer targetPort) {
        this.targetPort = targetPort;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public String getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public void setSchedulingStrategy(String schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getContainerIds() {
        return containerIds;
    }

    public void setContainerIds(List<String> containerIds) {
        this.containerIds = containerIds;
    }
}

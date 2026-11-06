package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {

    private String id;
    private String name;
    private String status; // READY, DRAINING, OFFLINE
    private int cpuCores;
    private long memoryMb;
    private double allocatedCpuCores;
    private long allocatedMemoryMb;
    private String ipAddress;
    private boolean localHost;
    private String dockerHostUri;
    private Map<String, String> labels = new HashMap<>();
    private int runningContainers;
    private String createdAt;

    public Node() {
        this.createdAt = Instant.now().toString();
        this.status = "READY";
    }

    public Node(String id, String name, String status, int cpuCores, long memoryMb) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.cpuCores = cpuCores;
        this.memoryMb = memoryMb;
        this.allocatedCpuCores = 0.0;
        this.allocatedMemoryMb = 0;
        this.ipAddress = "127.0.0.1";
        this.localHost = false;
        this.createdAt = Instant.now().toString();
    }

    public double getAvailableCpuCores() {
        return Math.max(0.0, cpuCores - allocatedCpuCores);
    }

    public long getAvailableMemoryMb() {
        return Math.max(0L, memoryMb - allocatedMemoryMb);
    }

    public double getCpuUtilizationPercent() {
        if (cpuCores <= 0) return 0.0;
        return Math.min(100.0, (allocatedCpuCores / (double) cpuCores) * 100.0);
    }

    public double getMemoryUtilizationPercent() {
        if (memoryMb <= 0) return 0.0;
        return Math.min(100.0, ((double) allocatedMemoryMb / (double) memoryMb) * 100.0);
    }

    public boolean canAccommodate(double requestedCpu, long requestedMemoryMb) {
        if (!"READY".equalsIgnoreCase(status)) {
            return false;
        }
        return getAvailableCpuCores() >= requestedCpu && getAvailableMemoryMb() >= requestedMemoryMb;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public long getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(long memoryMb) {
        this.memoryMb = memoryMb;
    }

    public double getAllocatedCpuCores() {
        return allocatedCpuCores;
    }

    public void setAllocatedCpuCores(double allocatedCpuCores) {
        this.allocatedCpuCores = allocatedCpuCores;
    }

    public long getAllocatedMemoryMb() {
        return allocatedMemoryMb;
    }

    public void setAllocatedMemoryMb(long allocatedMemoryMb) {
        this.allocatedMemoryMb = allocatedMemoryMb;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isLocalHost() {
        return localHost;
    }

    public void setLocalHost(boolean localHost) {
        this.localHost = localHost;
    }

    public String getDockerHostUri() {
        return dockerHostUri;
    }

    public void setDockerHostUri(String dockerHostUri) {
        this.dockerHostUri = dockerHostUri;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public int getRunningContainers() {
        return runningContainers;
    }

    public void setRunningContainers(int runningContainers) {
        this.runningContainers = runningContainers;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

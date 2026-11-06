package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterMetrics {

    private String timestamp;
    private int totalNodes;
    private int healthyNodes;
    private int drainingNodes;
    private int offlineNodes;

    private int totalApplications;
    private int runningApplications;

    private int totalContainers;
    private int runningContainers;
    private int stoppedContainers;
    private int crashedContainers;

    private int totalCpuCores;
    private double allocatedCpuCores;
    private double cpuUtilizationPercent;

    private long totalMemoryMb;
    private long allocatedMemoryMb;
    private double memoryUtilizationPercent;

    private boolean dockerConnected;

    public ClusterMetrics() {
        this.timestamp = Instant.now().toString();
    }

    // Getters and Setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public int getHealthyNodes() {
        return healthyNodes;
    }

    public void setHealthyNodes(int healthyNodes) {
        this.healthyNodes = healthyNodes;
    }

    public int getDrainingNodes() {
        return drainingNodes;
    }

    public void setDrainingNodes(int drainingNodes) {
        this.drainingNodes = drainingNodes;
    }

    public int getOfflineNodes() {
        return offlineNodes;
    }

    public void setOfflineNodes(int offlineNodes) {
        this.offlineNodes = offlineNodes;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getRunningApplications() {
        return runningApplications;
    }

    public void setRunningApplications(int runningApplications) {
        this.runningApplications = runningApplications;
    }

    public int getTotalContainers() {
        return totalContainers;
    }

    public void setTotalContainers(int totalContainers) {
        this.totalContainers = totalContainers;
    }

    public int getRunningContainers() {
        return runningContainers;
    }

    public void setRunningContainers(int runningContainers) {
        this.runningContainers = runningContainers;
    }

    public int getStoppedContainers() {
        return stoppedContainers;
    }

    public void setStoppedContainers(int stoppedContainers) {
        this.stoppedContainers = stoppedContainers;
    }

    public int getCrashedContainers() {
        return crashedContainers;
    }

    public void setCrashedContainers(int crashedContainers) {
        this.crashedContainers = crashedContainers;
    }

    public int getTotalCpuCores() {
        return totalCpuCores;
    }

    public void setTotalCpuCores(int totalCpuCores) {
        this.totalCpuCores = totalCpuCores;
    }

    public double getAllocatedCpuCores() {
        return allocatedCpuCores;
    }

    public void setAllocatedCpuCores(double allocatedCpuCores) {
        this.allocatedCpuCores = allocatedCpuCores;
    }

    public double getCpuUtilizationPercent() {
        return cpuUtilizationPercent;
    }

    public void setCpuUtilizationPercent(double cpuUtilizationPercent) {
        this.cpuUtilizationPercent = cpuUtilizationPercent;
    }

    public long getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public void setTotalMemoryMb(long totalMemoryMb) {
        this.totalMemoryMb = totalMemoryMb;
    }

    public long getAllocatedMemoryMb() {
        return allocatedMemoryMb;
    }

    public void setAllocatedMemoryMb(long allocatedMemoryMb) {
        this.allocatedMemoryMb = allocatedMemoryMb;
    }

    public double getMemoryUtilizationPercent() {
        return memoryUtilizationPercent;
    }

    public void setMemoryUtilizationPercent(double memoryUtilizationPercent) {
        this.memoryUtilizationPercent = memoryUtilizationPercent;
    }

    public boolean isDockerConnected() {
        return dockerConnected;
    }

    public void setDockerConnected(boolean dockerConnected) {
        this.dockerConnected = dockerConnected;
    }
}

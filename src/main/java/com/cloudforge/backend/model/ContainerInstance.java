package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerInstance {

    private String id;
    private String dockerContainerId;
    private String name;
    private String applicationId;
    private String applicationName;
    private String image;
    private String nodeId;
    private String nodeName;
    private String status; // PENDING, RUNNING, STOPPED, CRASHED, RESTARTING
    private Integer targetPort;
    private Integer hostPort;
    private double cpuPercent;
    private long memoryUsageMb;
    private long memoryLimitMb;
    private int restartCount;
    private boolean realDocker;
    private String createdAt;
    private String startedAt;
    private Integer exitCode;
    private String lastError;

    public ContainerInstance() {
        this.createdAt = Instant.now().toString();
        this.status = "PENDING";
        this.restartCount = 0;
        this.cpuPercent = 0.0;
        this.memoryUsageMb = 0;
    }

    public ContainerInstance(String id, String name, String applicationId, String applicationName, String image, String nodeId, String nodeName) {
        this();
        this.id = id;
        this.name = name;
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.image = image;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDockerContainerId() {
        return dockerContainerId;
    }

    public void setDockerContainerId(String dockerContainerId) {
        this.dockerContainerId = dockerContainerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public double getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(double cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public long getMemoryUsageMb() {
        return memoryUsageMb;
    }

    public void setMemoryUsageMb(long memoryUsageMb) {
        this.memoryUsageMb = memoryUsageMb;
    }

    public long getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public void setMemoryLimitMb(long memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }

    public int getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(int restartCount) {
        this.restartCount = restartCount;
    }

    public boolean isRealDocker() {
        return realDocker;
    }

    public void setRealDocker(boolean realDocker) {
        this.realDocker = realDocker;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}

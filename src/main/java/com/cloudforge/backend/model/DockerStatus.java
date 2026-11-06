package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerStatus {

    private boolean connected;
    private String dockerHost;
    private String serverVersion;
    private String apiVersion;
    private String osType;
    private String architecture;
    private int totalCpus;
    private long totalMemoryBytes;
    private int totalContainers;
    private int runningContainers;
    private int imagesCount;
    private String error;
    private String mode; // REAL_DOCKER or SIMULATED

    public DockerStatus() {
        this.connected = false;
        this.mode = "SIMULATED";
    }

    public static DockerStatus disconnected(String dockerHost, String error) {
        DockerStatus status = new DockerStatus();
        status.setConnected(false);
        status.setDockerHost(dockerHost);
        status.setError(error);
        status.setMode("SIMULATED");
        return status;
    }

    // Getters and Setters
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getDockerHost() {
        return dockerHost;
    }

    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public int getTotalCpus() {
        return totalCpus;
    }

    public void setTotalCpus(int totalCpus) {
        this.totalCpus = totalCpus;
    }

    public long getTotalMemoryBytes() {
        return totalMemoryBytes;
    }

    public void setTotalMemoryBytes(long totalMemoryBytes) {
        this.totalMemoryBytes = totalMemoryBytes;
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

    public int getImagesCount() {
        return imagesCount;
    }

    public void setImagesCount(int imagesCount) {
        this.imagesCount = imagesCount;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}

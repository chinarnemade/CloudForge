package com.cloudforge.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeployRequest {

    private String name;
    private String image;
    private int replicas = 1;
    private double requestedCpu = 0.5;
    private long requestedMemoryMb = 256;
    private Integer targetPort;
    private Integer hostPort;
    private Map<String, String> envVars = new HashMap<>();
    private String restartPolicy = "ALWAYS"; // ALWAYS, ON_FAILURE, NEVER
    private String schedulingStrategy = "LEAST_ALLOCATED"; // LEAST_ALLOCATED, BIN_PACKING, ROUND_ROBIN

    public DeployRequest() {}

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
}

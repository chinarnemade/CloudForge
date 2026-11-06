package com.cloudforge.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeCreateRequest {

    private String name;
    private int cpuCores = 4;
    private long memoryMb = 8192;
    private String ipAddress = "192.168.1.100";
    private Map<String, String> labels = new HashMap<>();

    public NodeCreateRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}

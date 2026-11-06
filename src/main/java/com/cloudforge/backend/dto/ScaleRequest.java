package com.cloudforge.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScaleRequest {

    private int replicas;

    public ScaleRequest() {}

    public ScaleRequest(int replicas) {
        this.replicas = replicas;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }
}

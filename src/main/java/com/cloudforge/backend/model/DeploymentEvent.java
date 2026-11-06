package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentEvent {

    private String id;
    private String eventType; // DEPLOY, SCALE, CONTAINER_START, CONTAINER_STOP, CONTAINER_CRASH, SELF_HEALING, NODE_DRAIN, SCHEDULING
    private String applicationId;
    private String applicationName;
    private String containerId;
    private String nodeId;
    private String title;
    private String message;
    private String severity; // INFO, SUCCESS, WARNING, ERROR
    private String timestamp;

    public DeploymentEvent() {
        this.id = "evt-" + UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now().toString();
        this.severity = "INFO";
    }

    public DeploymentEvent(String eventType, String title, String message, String severity) {
        this();
        this.eventType = eventType;
        this.title = title;
        this.message = message;
        this.severity = severity;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

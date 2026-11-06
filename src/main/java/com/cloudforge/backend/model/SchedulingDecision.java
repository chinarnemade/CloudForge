package com.cloudforge.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedulingDecision {

    private String applicationId;
    private String applicationName;
    private int replicaIndex;
    private String selectedNodeId;
    private String selectedNodeName;
    private String strategy;
    private boolean success;
    private String message;
    private List<NodeEvaluation> candidateEvaluations = new ArrayList<>();
    private String timestamp;

    public SchedulingDecision() {
        this.timestamp = Instant.now().toString();
    }

    public static class NodeEvaluation {
        private String nodeId;
        private String nodeName;
        private String status;
        private double availableCpu;
        private long availableMemoryMb;
        private double score;
        private boolean eligible;
        private String rejectionReason;

        public NodeEvaluation() {}

        public NodeEvaluation(String nodeId, String nodeName, String status, double availableCpu, long availableMemoryMb, double score, boolean eligible, String rejectionReason) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.status = status;
            this.availableCpu = availableCpu;
            this.availableMemoryMb = availableMemoryMb;
            this.score = score;
            this.eligible = eligible;
            this.rejectionReason = rejectionReason;
        }

        // Getters and Setters
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getAvailableCpu() { return availableCpu; }
        public void setAvailableCpu(double availableCpu) { this.availableCpu = availableCpu; }
        public long getAvailableMemoryMb() { return availableMemoryMb; }
        public void setAvailableMemoryMb(long availableMemoryMb) { this.availableMemoryMb = availableMemoryMb; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public boolean isEligible() { return eligible; }
        public void setEligible(boolean eligible) { this.eligible = eligible; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }

    // Getters and Setters
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

    public int getReplicaIndex() {
        return replicaIndex;
    }

    public void setReplicaIndex(int replicaIndex) {
        this.replicaIndex = replicaIndex;
    }

    public String getSelectedNodeId() {
        return selectedNodeId;
    }

    public void setSelectedNodeId(String selectedNodeId) {
        this.selectedNodeId = selectedNodeId;
    }

    public String getSelectedNodeName() {
        return selectedNodeName;
    }

    public void setSelectedNodeName(String selectedNodeName) {
        this.selectedNodeName = selectedNodeName;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NodeEvaluation> getCandidateEvaluations() {
        return candidateEvaluations;
    }

    public void setCandidateEvaluations(List<NodeEvaluation> candidateEvaluations) {
        this.candidateEvaluations = candidateEvaluations;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

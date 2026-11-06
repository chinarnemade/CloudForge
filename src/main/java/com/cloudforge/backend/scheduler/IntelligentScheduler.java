package com.cloudforge.backend.scheduler;

import com.cloudforge.backend.model.Application;
import com.cloudforge.backend.model.Node;
import com.cloudforge.backend.model.SchedulingDecision;
import com.cloudforge.backend.model.SchedulingDecision.NodeEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IntelligentScheduler implements WorkloadScheduler {

    private static final Logger log = LoggerFactory.getLogger(IntelligentScheduler.class);
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    @Override
    public SchedulingDecision scheduleReplica(
            Application application,
            int replicaIndex,
            List<Node> candidateNodes,
            String strategy
    ) {
        SchedulingDecision decision = new SchedulingDecision();
        decision.setApplicationId(application.getId());
        decision.setApplicationName(application.getName());
        decision.setReplicaIndex(replicaIndex);

        String activeStrategy = (strategy != null && !strategy.trim().isEmpty())
                ? strategy.toUpperCase().trim()
                : "LEAST_ALLOCATED";
        decision.setStrategy(activeStrategy);

        double reqCpu = application.getRequestedCpu();
        long reqMem = application.getRequestedMemoryMb();

        List<NodeEvaluation> evaluations = new ArrayList<>();
        List<ScoredNode> eligibleNodes = new ArrayList<>();

        for (Node node : candidateNodes) {
            boolean eligible = true;
            String rejectionReason = null;

            if (!"READY".equalsIgnoreCase(node.getStatus())) {
                eligible = false;
                rejectionReason = "Node is not READY (Current status: " + node.getStatus() + ")";
            } else if (node.getAvailableCpuCores() < reqCpu) {
                eligible = false;
                rejectionReason = String.format("Insufficient CPU: required %.2f cores, available %.2f cores", reqCpu, node.getAvailableCpuCores());
            } else if (node.getAvailableMemoryMb() < reqMem) {
                eligible = false;
                rejectionReason = String.format("Insufficient Memory: required %d MB, available %d MB", reqMem, node.getAvailableMemoryMb());
            }

            double score = 0.0;
            if (eligible) {
                double cpuRatio = node.getCpuCores() > 0 ? (node.getAvailableCpuCores() / node.getCpuCores()) : 0;
                double memRatio = node.getMemoryMb() > 0 ? ((double) node.getAvailableMemoryMb() / node.getMemoryMb()) : 0;

                switch (activeStrategy) {
                    case "BIN_PACKING":
                        // Inverse score: favor nodes that are most utilized to pack containers densely
                        score = 1.0 - ((cpuRatio * 0.5) + (memRatio * 0.5));
                        break;
                    case "ROUND_ROBIN":
                        score = 1.0;
                        break;
                    case "LEAST_ALLOCATED":
                    default:
                        // Favor nodes with maximum remaining headroom (spread workload)
                        score = (cpuRatio * 0.5) + (memRatio * 0.5);
                        break;
                }
                eligibleNodes.add(new ScoredNode(node, score));
            }

            evaluations.add(new NodeEvaluation(
                    node.getId(),
                    node.getName(),
                    node.getStatus(),
                    node.getAvailableCpuCores(),
                    node.getAvailableMemoryMb(),
                    Math.round(score * 100.0) / 100.0,
                    eligible,
                    rejectionReason
            ));
        }

        decision.setCandidateEvaluations(evaluations);

        if (eligibleNodes.isEmpty()) {
            decision.setSuccess(false);
            decision.setMessage(String.format("Scheduling failed: No candidate node satisfies CPU (%.2f cores) and Memory (%d MB) requirements.", reqCpu, reqMem));
            log.warn("Scheduling failed for app {} replica {}: insufficient cluster resources.", application.getName(), replicaIndex);
            return decision;
        }

        Node chosenNode;
        if ("ROUND_ROBIN".equalsIgnoreCase(activeStrategy)) {
            int idx = Math.abs(roundRobinCounter.getAndIncrement()) % eligibleNodes.size();
            chosenNode = eligibleNodes.get(idx).node;
        } else {
            // Sort by score descending (highest score first)
            eligibleNodes.sort((a, b) -> Double.compare(b.score, a.score));
            chosenNode = eligibleNodes.get(0).node;
        }

        decision.setSelectedNodeId(chosenNode.getId());
        decision.setSelectedNodeName(chosenNode.getName());
        decision.setSuccess(true);
        decision.setMessage(String.format("Scheduled on %s using %s strategy (Headroom: %.1f cores, %d MB free)",
                chosenNode.getName(), activeStrategy, chosenNode.getAvailableCpuCores(), chosenNode.getAvailableMemoryMb()));

        log.info("Replica #{} of application {} scheduled on node {} ({}) using strategy {}",
                replicaIndex, application.getName(), chosenNode.getName(), chosenNode.getId(), activeStrategy);

        return decision;
    }

    private record ScoredNode(Node node, double score) {}
}

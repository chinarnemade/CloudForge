package com.cloudforge.backend.scheduler;

import com.cloudforge.backend.model.Application;
import com.cloudforge.backend.model.Node;
import com.cloudforge.backend.model.SchedulingDecision;

import java.util.List;

public interface WorkloadScheduler {

    SchedulingDecision scheduleReplica(
            Application application,
            int replicaIndex,
            List<Node> candidateNodes,
            String strategy
    );
}

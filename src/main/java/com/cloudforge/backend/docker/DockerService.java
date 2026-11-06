package com.cloudforge.backend.docker;

import com.cloudforge.backend.model.DockerStatus;

import java.util.List;
import java.util.Map;

public interface DockerService {

    DockerStatus checkStatus();

    DockerStatus reconnect(String customHost);

    boolean isDockerAvailable();

    String createAndStartContainer(
            String image,
            String name,
            Integer hostPort,
            Integer containerPort,
            Map<String, String> envVars,
            double cpuCores,
            long memoryMb
    ) throws Exception;

    void stopContainer(String dockerContainerId) throws Exception;

    void startContainer(String dockerContainerId) throws Exception;

    void restartContainer(String dockerContainerId) throws Exception;

    void removeContainer(String dockerContainerId) throws Exception;

    String getContainerLogs(String dockerContainerId, int tailLines);

    ContainerStats getContainerStats(String dockerContainerId);

    boolean pullImage(String image);

    List<String> listRunningContainerIds();

    record ContainerStats(double cpuPercent, long memoryUsageMb, long memoryLimitMb) {}
}

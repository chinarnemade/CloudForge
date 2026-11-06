package com.cloudforge.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DockerConfig {

    @Value("${cloudforge.docker.auto-detect:true}")
    private boolean autoDetect;

    @Value("${cloudforge.docker.socket-path:}")
    private String customSocketPath;

    @Value("${cloudforge.docker.connection-timeout-seconds:5}")
    private int connectionTimeoutSeconds;

    @Value("${cloudforge.docker.response-timeout-seconds:45}")
    private int responseTimeoutSeconds;

    public String resolveDockerHost() {
        if (customSocketPath != null && !customSocketPath.trim().isEmpty()) {
            return customSocketPath.trim();
        }

        String envHost = System.getenv("DOCKER_HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            return envHost.trim();
        }

        if (autoDetect) {
            String userHome = System.getProperty("user.home");
            File macDockerSock = new File(userHome + "/.docker/run/docker.sock");
            if (macDockerSock.exists()) {
                return "unix://" + macDockerSock.getAbsolutePath();
            }

            File defaultSock = new File("/var/run/docker.sock");
            if (defaultSock.exists()) {
                return "unix:///var/run/docker.sock";
            }
        }

        // Default standard Docker socket
        String userHome = System.getProperty("user.home");
        File macDockerSock = new File(userHome + "/.docker/run/docker.sock");
        if (macDockerSock.exists()) {
            return "unix://" + macDockerSock.getAbsolutePath();
        }

        return "unix:///var/run/docker.sock";
    }

    public boolean isAutoDetect() {
        return autoDetect;
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public int getResponseTimeoutSeconds() {
        return responseTimeoutSeconds;
    }
}

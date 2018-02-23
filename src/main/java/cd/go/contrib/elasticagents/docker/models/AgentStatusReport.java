package cd.go.contrib.elasticagents.docker.models;

import java.util.List;
import java.util.Map;

public class AgentStatusReport {

    private final JobIdentifier jobIdentifier;
    private final String elasticAgentId;
    private final Long createdAt;
    private final String image;
    private final String command;
    private final String ipAddress;
    private final String logs;
    private final Map<String, String> environmentVariables;
    private final List<String> hosts;

    public AgentStatusReport(JobIdentifier jobIdentifier, String elasticAgentId, Long createdAt, String image, String command,
                             String ipAddress, String logs, Map<String, String> environmentVariables, List<String> hosts) {
        this.jobIdentifier = jobIdentifier;
        this.elasticAgentId = elasticAgentId;
        this.createdAt = createdAt;
        this.image = image;
        this.command = command;
        this.ipAddress = ipAddress;
        this.logs = logs;
        this.environmentVariables = environmentVariables;
        this.hosts = hosts;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getImage() {
        return image;
    }

    public String getCommand() {
        return command;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLogs() {
        return logs;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public List<String> getHosts() {
        return hosts;
    }
}

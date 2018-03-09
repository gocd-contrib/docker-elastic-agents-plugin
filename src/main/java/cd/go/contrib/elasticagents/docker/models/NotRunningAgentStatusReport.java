package cd.go.contrib.elasticagents.docker.models;

public class NotRunningAgentStatusReport {
    private final String entity;

    public NotRunningAgentStatusReport(JobIdentifier jobIdentifier) {
        this.entity = String.format("Job Identifier: %s", jobIdentifier.represent());
    }

    public NotRunningAgentStatusReport(String elasticAgentId) {
        this.entity = String.format("Elastic Agent ID: %s", elasticAgentId);
    }

    public String getEntity() {
        return entity;
    }
}

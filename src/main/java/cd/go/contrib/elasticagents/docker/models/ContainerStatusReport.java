package cd.go.contrib.elasticagents.docker.models;

import com.google.gson.annotations.Expose;

public class ContainerStatusReport {

    @Expose
    private String id;
    @Expose
    private String image;
    @Expose
    private String state;
    @Expose
    private Long createdAt;
    @Expose
    private final JobIdentifier jobIdentifier;
    @Expose
    private final String elasticAgentId;

    public ContainerStatusReport(String id, String image, String state, Long createdAt, JobIdentifier jobIdentifier, String elasticAgentId) {
        this.id = id;
        this.image = image;
        this.state = state;
        this.createdAt = createdAt;
        this.jobIdentifier = jobIdentifier;
        this.elasticAgentId = elasticAgentId;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getState() {
        return state;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }
}

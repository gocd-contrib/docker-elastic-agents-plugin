package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.docker.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.executors.AgentStatusReportExecutor;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class AgentStatusReportRequest {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Expose
    private String elasticAgentId;

    @Expose
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfile;

    public AgentStatusReportRequest() {
    }

    public AgentStatusReportRequest(String elasticAgentId, JobIdentifier jobIdentifier, Map<String, String> clusterProfileProperties) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfile = ClusterProfileProperties.fromConfiguration(clusterProfileProperties);
    }

    public static AgentStatusReportRequest fromJSON(String json) {
        return GSON.fromJson(json, AgentStatusReportRequest.class);
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public AgentStatusReportExecutor executor(PluginRequest pluginRequest, DockerContainers dockerContainers) throws IOException {
        return new AgentStatusReportExecutor(this, pluginRequest, dockerContainers, ViewBuilder.instance());
    }

    public ClusterProfileProperties getClusterProfile() {
        return clusterProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentStatusReportRequest that = (AgentStatusReportRequest) o;
        return Objects.equals(elasticAgentId, that.elasticAgentId) &&
                Objects.equals(jobIdentifier, that.jobIdentifier) &&
                Objects.equals(clusterProfile, that.clusterProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elasticAgentId, jobIdentifier, clusterProfile);
    }

    @Override
    public String toString() {
        return "AgentStatusReportRequest{" +
                "elasticAgentId='" + elasticAgentId + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                ", clusterProfile=" + clusterProfile +
                '}';
    }
}

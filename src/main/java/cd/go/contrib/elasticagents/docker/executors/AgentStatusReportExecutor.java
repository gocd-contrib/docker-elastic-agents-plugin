package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ExceptionMessage;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;

public class AgentStatusReportExecutor {
    private static final Logger LOG = Logger.getLoggerFor(AgentStatusReportExecutor.class);
    private final AgentStatusReportRequest request;
    private final PluginRequest pluginRequest;
    private final DockerContainers dockerContainers;
    private final ViewBuilder viewBuilder;

    public AgentStatusReportExecutor(AgentStatusReportRequest request, PluginRequest pluginRequest,
                                     DockerContainers dockerContainers, ViewBuilder viewBuilder) {
        this.request = request;
        this.pluginRequest = pluginRequest;
        this.dockerContainers = dockerContainers;
        this.viewBuilder = viewBuilder;
    }

    public GoPluginApiResponse execute() throws Exception {
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));

        AgentStatusReport agentStatusReport;
        try {
            if (StringUtils.isNotBlank(elasticAgentId)) {
                agentStatusReport = dockerContainers.getAgentStatusReport(elasticAgentId, pluginRequest.getPluginSettings());
            } else {
                agentStatusReport = dockerContainers.getAgentStatusReport(jobIdentifier, pluginRequest.getPluginSettings());
            }

            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("agent-status-report.template.ftlh"), agentStatusReport);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            LOG.debug("Exception while generating agent status report", e);
            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("error.template.ftlh"), new ExceptionMessage(e));

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        }
    }
}

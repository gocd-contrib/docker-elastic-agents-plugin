package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ExceptionMessage;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.models.NotRunningAgentStatusReport;
import cd.go.contrib.elasticagents.docker.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Optional;

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

        try {
            if (StringUtils.isNotBlank(elasticAgentId)) {
                return getStatusReportUsingElasticAgentId(elasticAgentId);
            }
            return getStatusReportUsingJobIdentifier(jobIdentifier);
        } catch (Exception e) {
            LOG.debug("Exception while generating agent status report", e);
            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("error.template.ftlh"), new ExceptionMessage(e));

            return constructResponseForReport(statusReportView);
        }
    }

    private GoPluginApiResponse getStatusReportUsingJobIdentifier(JobIdentifier jobIdentifier) throws Exception {
        Optional<DockerContainer> dockerContainer = dockerContainers.find(jobIdentifier);
        if (dockerContainer.isPresent()) {
            AgentStatusReport agentStatusReport = dockerContainers.getAgentStatusReport(pluginRequest.getPluginSettings(), dockerContainer.get());
            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("agent-status-report.template.ftlh"), agentStatusReport);
            return constructResponseForReport(statusReportView);
        }

        return containerNotFoundApiResponse(jobIdentifier);
    }

    private GoPluginApiResponse getStatusReportUsingElasticAgentId(String elasticAgentId) throws Exception {
        Optional<DockerContainer> dockerContainer = Optional.ofNullable(dockerContainers.find(elasticAgentId));
        if (dockerContainer.isPresent()) {
            AgentStatusReport agentStatusReport = dockerContainers.getAgentStatusReport(pluginRequest.getPluginSettings(), dockerContainer.get());
            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("agent-status-report.template.ftlh"), agentStatusReport);
            return constructResponseForReport(statusReportView);
        }
        return containerNotFoundApiResponse(elasticAgentId);
    }

    private GoPluginApiResponse constructResponseForReport(String statusReportView) {
        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }

    private GoPluginApiResponse containerNotFoundApiResponse(JobIdentifier jobIdentifier) throws IOException, TemplateException {
        Template template = viewBuilder.getTemplate("not-running-agent-status-report.template.ftlh");
        final String statusReportView = viewBuilder.build(template, new NotRunningAgentStatusReport(jobIdentifier));
        return constructResponseForReport(statusReportView);
    }

    private GoPluginApiResponse containerNotFoundApiResponse(String elasticAgentId) throws IOException, TemplateException {
        Template template = viewBuilder.getTemplate("not-running-agent-status-report.template.ftlh");
        final String statusReportView = viewBuilder.build(template, new NotRunningAgentStatusReport(elasticAgentId));
        return constructResponseForReport(statusReportView);
    }
}

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.RequestExecutor;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class StatusReportExecutor implements RequestExecutor {

    private final PluginRequest pluginRequest;
    private final DockerContainers dockerContainers;
    private final ViewBuilder viewBuilder;

    public StatusReportExecutor(PluginRequest pluginRequest, DockerContainers dockerContainers, ViewBuilder viewBuilder) {
        this.pluginRequest = pluginRequest;
        this.dockerContainers = dockerContainers;
        this.viewBuilder = viewBuilder;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        LOG.info("[status-report] Generating status report");

        StatusReport statusReport = dockerContainers.getStatusReport(pluginRequest.getPluginSettings());

        final Template template = viewBuilder.getTemplate("status-report.template.ftlh");
        final String statusReportView = viewBuilder.build(template, statusReport);

        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }
}

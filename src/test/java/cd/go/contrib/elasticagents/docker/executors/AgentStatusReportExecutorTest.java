package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.PluginSettings;
import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ExceptionMessage;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgentStatusReportExecutorTest {

    @Mock
    private PluginRequest pluginRequest;
    @Mock
    private PluginSettings pluginSettings;
    @Mock
    private DockerContainers dockerContainers;
    @Mock
    private ViewBuilder viewBuilder;
    @Mock
    private Template template;

    @Test
    public void shouldGetAgentStatusReportWithElasticAgentId() throws Exception {
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest("elastic-agent-id", null);
        AgentStatusReport agentStatusReport = new AgentStatusReport(null, "elastic-agent-id", null, null, null, null, null, new HashMap<>(), new ArrayList<>());
        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(dockerContainers.getAgentStatusReport("elastic-agent-id", pluginSettings)).thenReturn(agentStatusReport);
        when(viewBuilder.getTemplate("agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, agentStatusReport)).thenReturn("agentStatusReportView");

        GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, dockerContainers, viewBuilder)
                .execute();

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "agentStatusReportView");
        assertThat(goPluginApiResponse.responseCode(), is(200));
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    @Test
    public void shouldGetAgentStatusReportWithJobIdentifier() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(null, jobIdentifier);
        AgentStatusReport agentStatusReport = new AgentStatusReport(jobIdentifier, "elastic-agent-id", null, null, null, null, null, new HashMap<>(), new ArrayList<>());
        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(dockerContainers.getAgentStatusReport(jobIdentifier, pluginSettings)).thenReturn(agentStatusReport);
        when(viewBuilder.getTemplate("agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, agentStatusReport)).thenReturn("agentStatusReportView");

        GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, dockerContainers, viewBuilder)
                .execute();

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "agentStatusReportView");
        assertThat(goPluginApiResponse.responseCode(), is(200));
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    @Test
    public void shouldRenderErrorViewWhenAgentStatusReportGeneratesException() throws Exception {
        RuntimeException exception = new RuntimeException();
        JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(null, jobIdentifier);
        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(dockerContainers.getAgentStatusReport(jobIdentifier, pluginSettings)).thenThrow(exception);
        when(viewBuilder.getTemplate("error.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(eq(template), any(ExceptionMessage.class))).thenReturn("errorView");

        GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, dockerContainers, viewBuilder)
                .execute();

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "errorView");
        assertThat(goPluginApiResponse.responseCode(), is(200));
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

}

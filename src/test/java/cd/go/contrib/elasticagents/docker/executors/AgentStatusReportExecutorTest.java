package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.models.NotRunningAgentStatusReport;
import cd.go.contrib.elasticagents.docker.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.*;

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
    private DockerContainers dockerContainers;
    @Mock
    private ViewBuilder viewBuilder;
    @Mock
    private Template template;
    private Map<String, String> clusterProfileConfigurations;
    private ClusterProfileProperties clusterProfile;

    @Before
    public void setup() {
        clusterProfileConfigurations = Collections.singletonMap("go_server_url", "http://go-server-url/go");
        clusterProfile = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);
    }

    @Test
    public void shouldGetAgentStatusReportWithElasticAgentId() throws Exception {
        String agentId = "elastic-agent-id";
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(agentId, null, clusterProfileConfigurations);
        AgentStatusReport agentStatusReport = new AgentStatusReport(null, agentId, null, null, null, null, null, new HashMap<>(), new ArrayList<>());

        DockerContainer dockerContainer = new DockerContainer("id", "name", new JobIdentifier(), new Date(), new HashMap<>(), null);
        when(dockerContainers.find(agentId)).thenReturn(dockerContainer);
        when(dockerContainers.getAgentStatusReport(clusterProfile, dockerContainer)).thenReturn(agentStatusReport);
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
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(null, jobIdentifier, clusterProfileConfigurations);
        AgentStatusReport agentStatusReport = new AgentStatusReport(jobIdentifier, "elastic-agent-id", null, null, null, null, null, new HashMap<>(), new ArrayList<>());

        DockerContainer dockerContainer = new DockerContainer("id", "name", jobIdentifier, new Date(), new HashMap<>(), null);
        when(dockerContainers.find(jobIdentifier)).thenReturn(Optional.ofNullable(dockerContainer));
        when(dockerContainers.getAgentStatusReport(clusterProfile, dockerContainer)).thenReturn(agentStatusReport);
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
    public void shouldRenderContainerNotFoundAgentStatusReportViewWhenNoContainerIsRunningForProvidedJobIdentifier() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);

        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(null, jobIdentifier, clusterProfileConfigurations);

        when(dockerContainers.find(jobIdentifier)).thenReturn(Optional.empty());
        when(viewBuilder.getTemplate("not-running-agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(eq(template), any(NotRunningAgentStatusReport.class))).thenReturn("errorView");

        GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, dockerContainers, viewBuilder)
                .execute();

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "errorView");
        assertThat(goPluginApiResponse.responseCode(), is(200));
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    @Test
    public void shouldRenderContainerNotFoundAgentStatusReportViewWhenNoContainerIsRunningForProvidedElasticAgentId() throws Exception {
        String elasticAgentId = "elastic-agent-id";
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(elasticAgentId, null, clusterProfileConfigurations);

        when(dockerContainers.find(elasticAgentId)).thenReturn(null);
        when(viewBuilder.getTemplate("not-running-agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(eq(template), any(NotRunningAgentStatusReport.class))).thenReturn("errorView");

        GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, dockerContainers, viewBuilder)
                .execute();

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "errorView");
        assertThat(goPluginApiResponse.responseCode(), is(200));
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }
}

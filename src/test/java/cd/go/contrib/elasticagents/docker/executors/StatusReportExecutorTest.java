package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.PluginSettings;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusReportExecutorTest {

    @Mock
    private PluginRequest pluginRequest;

    @Mock
    private PluginSettings pluginSettings;

    @Mock
    private ViewBuilder viewBuilder;

    @Mock
    private DockerContainers dockerContainers;

    @Mock
    private Template template;

    @Test
    public void shouldGetStatusReport() throws Exception {
        StatusReport statusReport = aStatusReport();
        when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
        when(dockerContainers.getStatusReport(pluginSettings)).thenReturn(statusReport);
        when(viewBuilder.getTemplate("status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, statusReport)).thenReturn("statusReportView");
        StatusReportExecutor statusReportExecutor = new StatusReportExecutor(pluginRequest, dockerContainers, viewBuilder);

        GoPluginApiResponse goPluginApiResponse = statusReportExecutor.execute();

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "statusReportView");
        assertThat(goPluginApiResponse.responseCode(), is(200));
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    private StatusReport aStatusReport() {
        return new StatusReport("os", "x86_64", "0.1.2", 2, "100M", new ArrayList<>());
    }

}

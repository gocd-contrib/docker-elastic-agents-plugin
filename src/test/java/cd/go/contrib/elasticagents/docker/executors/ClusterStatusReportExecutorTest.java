package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.requests.ClusterStatusReportRequest;
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
public class ClusterStatusReportExecutorTest {

    @Mock
    private ClusterStatusReportRequest clusterStatusReportRequest;

    @Mock
    private ClusterProfileProperties clusterProfile;

    @Mock
    private ViewBuilder viewBuilder;

    @Mock
    private DockerContainers dockerContainers;

    @Mock
    private Template template;

    @Test
    public void shouldGetStatusReport() throws Exception {
        StatusReport statusReport = aStatusReport();
        when(clusterStatusReportRequest.getClusterProfile()).thenReturn(clusterProfile);
        when(dockerContainers.getStatusReport(clusterProfile)).thenReturn(statusReport);
        when(viewBuilder.getTemplate("status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, statusReport)).thenReturn("statusReportView");
        ClusterStatusReportExecutor statusReportExecutor = new ClusterStatusReportExecutor(clusterStatusReportRequest, dockerContainers, viewBuilder);

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

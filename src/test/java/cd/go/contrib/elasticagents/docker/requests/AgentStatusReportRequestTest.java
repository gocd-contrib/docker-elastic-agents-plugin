package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.docker.utils.JobIdentifierMother;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AgentStatusReportRequestTest {

    @Test
    public void shouldDeserializeFromJSON() {
        JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("elastic_agent_id", "some-id");
        jsonObject.add("job_identifier", jobIdentifierJson);

        AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());

        AgentStatusReportRequest expected = new AgentStatusReportRequest("some-id", JobIdentifierMother.get(), null);
        assertThat(agentStatusReportRequest, is(expected));
    }

    @Test
    public void shouldDeserializeFromJSONWithClusterProfile() {
        JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("elastic_agent_id", "some-id");
        jsonObject.add("job_identifier", jobIdentifierJson);
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("GoServerUrl", "https://foo.com/go");
        clusterJSON.addProperty("DockerUri", "unix:///var/run/docker.sock");
        jsonObject.add("cluster_profile_properties", clusterJSON);

        AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());

        Map<String, String> expectedClusterProfile = new HashMap<>();
        expectedClusterProfile.put("GoServerUrl", "https://foo.com/go");
        expectedClusterProfile.put("DockerUri", "unix:///var/run/docker.sock");

        AgentStatusReportRequest expected = new AgentStatusReportRequest("some-id", JobIdentifierMother.get(), expectedClusterProfile);
        assertThat(agentStatusReportRequest, is(expected));
    }
}

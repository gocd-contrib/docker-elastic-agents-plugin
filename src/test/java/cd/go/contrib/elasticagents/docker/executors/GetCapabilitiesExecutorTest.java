package cd.go.contrib.elasticagents.docker.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GetCapabilitiesExecutorTest {

    @Test
    public void shouldReturnResponse() throws Exception {
        GoPluginApiResponse response = new GetCapabilitiesExecutor().execute();

        assertThat(response.responseCode(), is(200));
        JSONObject expected = new JSONObject().put("supports_status_report", true);
        expected.put("supports_agent_status_report", true);
        JSONAssert.assertEquals(expected, new JSONObject(response.responseBody()), true);
    }

}

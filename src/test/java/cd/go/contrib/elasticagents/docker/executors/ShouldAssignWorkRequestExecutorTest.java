package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.Agent;
import cd.go.contrib.elasticagents.docker.BaseTest;
import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ShouldAssignWorkRequestExecutorTest extends BaseTest {

    private DockerContainers agentInstances;
    private DockerContainer dockerContainer;
    private final String environment = "production";
    private Map<String, String> properties = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        agentInstances = new DockerContainers();
        properties.put("foo", "bar");
        properties.put("Image", "gocdcontrib/ubuntu-docker-elastic-agent");
        dockerContainer = agentInstances.create(new CreateAgentRequest(UUID.randomUUID().toString(), properties, environment), createSettings());
        containers.add(dockerContainer.name());
    }

    @Test
    public void shouldAssignWorkToContainerWithMatchingEnvironmentNameAndProperties() throws Exception {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(dockerContainer.name(), null, null, null), environment, properties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, null).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("true"));
    }

    @Test
    public void shouldNotAssignWorkToContainerWithDifferentEnvironmentName() throws Exception {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(dockerContainer.name(), null, null, null), "FooEnv", properties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, null).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }

    @Test
    public void shouldNotAssignWorkToContainerWithDifferentProperties() throws Exception {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(dockerContainer.name(), null, null, null), environment, null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, null).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }
}
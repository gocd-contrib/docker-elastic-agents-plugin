/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private DockerContainer instance;
    private final String environment = "production";
    private Map<String, String> properties = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        agentInstances = new DockerContainers();
        properties.put("foo", "bar");
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        instance = agentInstances.create(new CreateAgentRequest(UUID.randomUUID().toString(), properties, environment), createSettings());
        containers.add(instance.name());
    }

    @Test
    public void shouldAssignWorkToContainerWithMatchingEnvironmentNameAndProperties() throws Exception {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), environment, properties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, null).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("true"));
    }

    @Test
    public void shouldNotAssignWorkToContainerWithDifferentEnvironmentName() throws Exception {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), "FooEnv", properties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, null).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }

    @Test
    public void shouldNotAssignWorkToContainerWithDifferentProperties() throws Exception {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), environment, null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, null).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }
}

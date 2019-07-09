/*
 * Copyright 2019 ThoughtWorks, Inc.
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

import cd.go.contrib.elasticagents.docker.*;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
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
import static org.mockito.Mockito.mock;

public class ShouldAssignWorkRequestExecutorTest extends BaseTest {

    private DockerContainers agentInstances;
    private DockerContainer instance;
    private final String environment = "production";
    private final JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 1L);
    private Map<String, String> properties = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        agentInstances = new DockerContainers();
        properties.put("foo", "bar");
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        instance = agentInstances.create(new CreateAgentRequest(UUID.randomUUID().toString(), properties, environment, jobIdentifier, clusterProfiles), pluginRequest, mock(ConsoleLogAppender.class));
        containers.add(instance.name());
    }

    @Test
    public void shouldAssignWorkToContainerWithSameJobIdentifier() {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), environment, jobIdentifier, null, null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("true"));
    }

    @Test
    public void shouldNotAssignWorkToContainerWithDifferentJobIdentifier() {
        JobIdentifier otherJobId = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 2L);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(instance.name(), null, null, null), environment, otherJobId, null,null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }

    @Test
    public void shouldNotAssignWorkIfInstanceIsNotFound() {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent("unknown-name", null, null, null), environment, jobIdentifier, null,null);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances).execute();
        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), is("false"));
    }
}

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

package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DockerContainersTest extends BaseTest {

    private CreateAgentRequest request;
    private DockerContainers dockerContainers;
    private ClusterProfileProperties settings;
    private final JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 1L);

    @Before
    public void setUp() throws Exception {
        settings = createSettings();
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        request = new CreateAgentRequest("key", properties, "production", jobIdentifier, settings);
        dockerContainers = new DockerContainers();
    }

    @Test
    public void shouldCreateADockerInstance() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest);
        containers.add(container.name());
        assertContainerExist(container.name());
    }

    @Test
    public void shouldUpdateServerHealthMessageWithEmptyListWhileCreatingADockerInstance() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        DockerContainer container = dockerContainers.create(request, pluginRequest);
        containers.add(container.name());

        verify(pluginRequest).addServerHealthMessage(new ArrayList<>());
        assertContainerExist(container.name());
    }

    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        DockerContainer container = dockerContainers.create(request, pluginRequest);
        containers.add(container.name());

        dockerContainers.terminate(container.name(), settings);

        assertContainerDoesNotExist(container.name());
    }

    @Test
    public void shouldRefreshAllAgentInstancesAtStartUp() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        DockerContainers dockerContainers = new DockerContainers();

        ClusterProfileProperties profileProperties = createSettings();
        dockerContainers.refreshAll(profileProperties);
        assertThat(dockerContainers.find(container.name()), is(container));
    }

    @Test
    public void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerContainers dockerContainers = new DockerContainers();
        ClusterProfileProperties profileProperties = createSettings();
        dockerContainers.refreshAll(profileProperties);

        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        dockerContainers.refreshAll(profileProperties);

        assertEquals(dockerContainers.find(container.name()), null);
    }

    @Test
    public void shouldNotListTheContainerIfItIsCreatedBeforeTimeout() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        ClusterProfileProperties profileProperties = createSettings();

        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerContainers.refreshAll(profileProperties);

        Agents filteredDockerContainers = dockerContainers.instancesCreatedAfterTimeout(createSettings(), new Agents(Arrays.asList(new Agent(container.name(), null, null, null))));

        assertFalse(filteredDockerContainers.containsAgentWithId(container.name()));
    }

    @Test
    public void shouldListTheContainerIfItIsNotCreatedBeforeTimeout() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        ClusterProfileProperties profileProperties = createSettings();

        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerContainers.refreshAll(profileProperties);

        Agents filteredDockerContainers = dockerContainers.instancesCreatedAfterTimeout(createSettings(), new Agents(Arrays.asList(new Agent(container.name(), null, null, null))));

        assertTrue(filteredDockerContainers.containsAgentWithId(container.name()));
    }

    @Test
    public void shouldNotCreateContainersIfMaxLimitIsReached() throws Exception {

        // do not allow any containers
        settings.setMaxDockerContainers(0);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);

        DockerContainer dockerContainer = dockerContainers.create(request, pluginRequest);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertNull(dockerContainer);

        // allow only one container
        settings.setMaxDockerContainers(1);
        dockerContainer = dockerContainers.create(request, pluginRequest);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertNotNull(dockerContainer);

        dockerContainer = dockerContainers.create(request, pluginRequest);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertNull(dockerContainer);
    }

    @Test
    public void shouldAddAWarningToTheServerHealthMessagesIfAgentsCannotBeCreated() throws Exception {
        // do not allow any containers
        settings.setMaxDockerContainers(0);

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);

        dockerContainers.create(request, pluginRequest);
        dockerContainers.create(new CreateAgentRequest("key", new HashMap<>(), "production", new JobIdentifier("up42", 2L, "foo", "stage", "1", "job2", 1L), settings), pluginRequest);
        ArrayList<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("type", "warning");
        message.put("message", "The number of containers currently running is currently at the maximum permissible limit, \"0\". Not creating more containers for jobs: up42/2/stage/1/job, up42/2/stage/1/job2.");
        messages.add(message);
        verify(pluginRequest).addServerHealthMessage(messages);
    }

    @Test
    public void shouldTerminateUnregisteredContainersAfterTimeout() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        DockerContainer container = dockerContainers.create(request, pluginRequest);

        assertTrue(dockerContainers.hasInstance(container.name()));
        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerContainers.terminateUnregisteredInstances(createSettings(), new Agents());
        assertFalse(dockerContainers.hasInstance(container.name()));
        assertContainerDoesNotExist(container.name());
    }

    @Test
    public void shouldNotTerminateUnregistredContainersBeforeTimeout() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        DockerContainer container = dockerContainers.create(request, pluginRequest);
        containers.add(container.name());

        assertTrue(dockerContainers.hasInstance(container.name()));
        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerContainers.terminateUnregisteredInstances(createSettings(), new Agents());
        assertTrue(dockerContainers.hasInstance(container.name()));
        assertContainerExist(container.name());
    }

    @Test
    public void shouldGetStatusReport() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        DockerContainer container = dockerContainers.create(request, pluginRequest);
        containers.add(container.name());

        StatusReport statusReport = dockerContainers.getStatusReport(settings);

        assertThat(statusReport, is(notNullValue()));
        assertThat(statusReport.getContainerStatusReports(), hasSize(1));
    }

    @Test
    public void shouldGetAgentStatusReportUsingDockerContainer() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(settings);
        DockerContainer container = dockerContainers.create(request, pluginRequest);
        containers.add(container.name());

        AgentStatusReport agentStatusReport = dockerContainers.getAgentStatusReport(settings, container);

        assertThat(agentStatusReport, is(notNullValue()));
        assertThat(agentStatusReport.getElasticAgentId(), is(container.name()));
        assertThat(agentStatusReport.getJobIdentifier(), is(request.jobIdentifier()));
    }
}

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

package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerContainersTest extends BaseTest {

    private CreateAgentRequest request;
    private DockerContainers dockerContainers;
    private PluginSettings settings;

    @Before
    public void setUp() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        request = new CreateAgentRequest("key", properties, "production");
        dockerContainers = new DockerContainers();
        settings = createSettings();
    }

    @Test
    public void shouldCreateADockerInstance() throws Exception {
        DockerContainer container = dockerContainers.create(request, settings);
        containers.add(container.name());
        assertContainerExist(container.name());
    }

    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerContainer container = dockerContainers.create(request, settings);
        containers.add(container.name());

        dockerContainers.terminate(container.name(), settings);

        assertContainerDoesNotExist(container.name());
    }

    @Test
    public void shouldRefreshAllAgentInstancesAtStartUp() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        DockerContainers dockerContainers = new DockerContainers();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        dockerContainers.refreshAll(pluginRequest);
        assertThat(dockerContainers.find(container.name()), is(container));
    }

    @Test
    public void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerContainers dockerContainers = new DockerContainers();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());
        dockerContainers.refreshAll(pluginRequest);

        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        dockerContainers.refreshAll(pluginRequest);

        assertEquals(dockerContainers.find(container.name()), null);
    }

    @Test
    public void shouldNotListTheContainerIfItIsCreatedBeforeTimeout() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());

        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerContainers.refreshAll(pluginRequest);

        Agents filteredDockerContainers = dockerContainers.instancesCreatedAfterTimeout(createSettings(), new Agents(Arrays.asList(new Agent(container.name(), null, null, null))));

        assertFalse(filteredDockerContainers.containsAgentWithId(container.name()));
    }

    @Test
    public void shouldListTheContainerIfItIsNotCreatedBeforeTimeout() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.name());

        PluginRequest pluginRequest = mock(PluginRequest.class);
        when(pluginRequest.getPluginSettings()).thenReturn(createSettings());

        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerContainers.refreshAll(pluginRequest);

        Agents filteredDockerContainers = dockerContainers.instancesCreatedAfterTimeout(createSettings(), new Agents(Arrays.asList(new Agent(container.name(), null, null, null))));

        assertTrue(filteredDockerContainers.containsAgentWithId(container.name()));
    }

    @Test
    public void shouldNotCreateContainersIfMaxLimitIsReached() throws Exception {
        PluginSettings settings = createSettings();

        // do not allow any containers
        settings.setMaxDockerContainers(0);

        DockerContainer dockerContainer = dockerContainers.create(request, settings);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertNull(dockerContainer);

        // allow only one container
        settings.setMaxDockerContainers(1);
        dockerContainer = dockerContainers.create(request, settings);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertNotNull(dockerContainer);

        dockerContainer = dockerContainers.create(request, settings);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertNull(dockerContainer);
    }

    @Test
    public void shouldTerminateUnregistredContainersAfterTimeout() throws Exception {
        DockerContainer container = dockerContainers.create(request, settings);

        assertTrue(dockerContainers.hasInstance(container.name()));
        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerContainers.terminateUnregisteredInstances(createSettings(), new Agents());
        assertFalse(dockerContainers.hasInstance(container.name()));
        assertContainerDoesNotExist(container.name());
    }

    @Test
    public void shouldNotTerminateUnregistredContainersBeforeTimeout() throws Exception {
        DockerContainer container = dockerContainers.create(request, settings);
        containers.add(container.name());

        assertTrue(dockerContainers.hasInstance(container.name()));
        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerContainers.terminateUnregisteredInstances(createSettings(), new Agents());
        assertTrue(dockerContainers.hasInstance(container.name()));
        assertContainerExist(container.name());
    }
}

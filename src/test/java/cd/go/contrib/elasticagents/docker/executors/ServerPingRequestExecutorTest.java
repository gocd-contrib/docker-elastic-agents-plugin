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
import cd.go.contrib.elasticagents.docker.requests.ServerPingRequest;
import org.joda.time.Period;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.*;

import static cd.go.contrib.elasticagents.docker.Agent.ConfigState.Disabled;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class ServerPingRequestExecutorTest extends BaseTest {

    @Test
    public void testShouldDisableIdleAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(Arrays.asList(new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled)));
        DockerContainers agentInstances = new DockerContainers();

        PluginRequest pluginRequest = mock(PluginRequest.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(createSettings()));
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);

        final Collection<Agent> values = agents.agents();
        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createSettings().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(serverPingRequest, dockerContainers, pluginRequest).execute();
        verify(pluginRequest).disableAgents(argThat(collectionMatches(values)));
    }

    private ArgumentMatcher<Collection<Agent>> collectionMatches(final Collection<Agent> values) {
        return new ArgumentMatcher<Collection<Agent>>() {
            @Override
            public boolean matches(Collection<Agent> argument) {
                return new ArrayList<>(argument).equals(new ArrayList<>(values));
            }
        };
    }

    @Test
    public void testShouldTerminateDisabledAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(Arrays.asList(new Agent(agentId, Agent.AgentState.Idle, Agent.BuildState.Idle, Disabled)));
        DockerContainers agentInstances = new DockerContainers();

        PluginRequest pluginRequest = mock(PluginRequest.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(createSettings()));
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);
        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createSettings().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(serverPingRequest, dockerContainers, pluginRequest).execute();
        final Collection<Agent> values = agents.agents();
        verify(pluginRequest, atLeast(1)).deleteAgents(argThat(collectionMatches(values)));
    }

    @Test
    public void testShouldTerminateInstancesThatNeverAutoRegistered() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(createSettings()));
        when(pluginRequest.listAgents()).thenReturn(new Agents());
        verifyNoMoreInteractions(pluginRequest);

        DockerContainers agentInstances = new DockerContainers();
        agentInstances.clock = new Clock.TestClock().forward(Period.minutes(11));
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        DockerContainer container = agentInstances.create(new CreateAgentRequest(null, properties, null, new JobIdentifier(), createSettings()), pluginRequest);
        containers.add(container.name());

        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createSettings().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(serverPingRequest, dockerContainers, pluginRequest).execute();

        assertFalse(agentInstances.hasInstance(container.name()));
    }

    @Test
    public void shouldDeleteAgentFromConfigWhenCorrespondingContainerIsNotPresent() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.allClusterProfileProperties()).thenReturn(Arrays.asList(createSettings()));
        when(pluginRequest.listAgents()).thenReturn(new Agents(Arrays.asList(new Agent("foo", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled))));
        verifyNoMoreInteractions(pluginRequest);

        DockerContainers agentInstances = new DockerContainers();
        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createSettings().uuid(), agentInstances);
        }};

        ServerPingRequestExecutor serverPingRequestExecutor = new ServerPingRequestExecutor(serverPingRequest, dockerContainers, pluginRequest);
        serverPingRequestExecutor.execute();
    }
}

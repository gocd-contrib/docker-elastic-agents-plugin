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

import cd.go.contrib.elasticagents.Agent;
import cd.go.contrib.elasticagents.Agents;
import cd.go.contrib.elasticagents.docker.*;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.requests.ServerPingRequest;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import org.joda.time.Period;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ServerPingRequestExecutorTest extends BaseTest {

    @Test
    public void testShouldDisableIdleAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        Agents agents = new Agents(Arrays.asList(new Agent(agentId, "Idle", "Idle", "Enabled")));

        DockerContainers containers = new DockerContainers();
        GoApplicationAccessor goAccessor = mock(GoApplicationAccessor.class);
        when(goAccessor.submit(any(GoApiRequest.class))).thenReturn(DefaultGoApiResponse.success(""));
        new ServerPingRequestExecutor(new ServerPingRequest(agents), containers, createSettings(), goAccessor).execute();
        ArgumentCaptor<GoApiRequest> captor = ArgumentCaptor.forClass(GoApiRequest.class);
        verify(goAccessor).submit(captor.capture());

        assertEquals("go.processor.elasticagent.disable-agent", captor.getValue().api());
        assertEquals("1.0", captor.getValue().apiVersion());
        JSONAssert.assertEquals("[{\"agent_id\":\"" + agentId + "\",\"agent_state\":\"Idle\",\"build_state\":\"Idle\",\"config_state\":\"Enabled\"}]", captor.getValue().requestBody(), true);

        assertTrue(captor.getValue().requestHeaders().isEmpty());
        assertTrue(captor.getValue().requestParameters().isEmpty());
    }

    @Test
    public void testShouldTerminateDisabledAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        Agents agents = new Agents(Arrays.asList(new Agent(agentId, "Idle", "Idle", "Disabled")));

        DockerContainers containers = new DockerContainers();
        GoApplicationAccessor goAccessor = mock(GoApplicationAccessor.class);
        when(goAccessor.submit(any(GoApiRequest.class))).thenReturn(DefaultGoApiResponse.success(""));
        new ServerPingRequestExecutor(new ServerPingRequest(agents), containers, createSettings(), goAccessor).execute();
        ArgumentCaptor<GoApiRequest> captor = ArgumentCaptor.forClass(GoApiRequest.class);
        verify(goAccessor).submit(captor.capture());

        assertEquals("go.processor.elasticagent.delete-agent", captor.getValue().api());
        assertEquals("1.0", captor.getValue().apiVersion());
        JSONAssert.assertEquals("[{\"agent_id\":\"" + agentId + "\",\"agent_state\":\"Idle\",\"build_state\":\"Idle\",\"config_state\":\"Disabled\"}]", captor.getValue().requestBody(), true);

        assertTrue(captor.getValue().requestHeaders().isEmpty());
        assertTrue(captor.getValue().requestParameters().isEmpty());
    }

    @Test
    public void testShouldTerminateInstancesThatNeverAutoRegistered() throws Exception {
        PluginSettings settings = spy(createSettings());
        when(settings.getAutoRegisterPeriod()).thenReturn(new Period().withMinutes(0));

        DockerContainers dockerContainers = new DockerContainers();
        DockerContainer container = dockerContainers.create(new CreateAgentRequest(null, new HashSet<String>(), null), settings);
        containers.add(container.id());

        ServerPingRequestExecutor serverPingRequestExecutor = new ServerPingRequestExecutor(new ServerPingRequest(new Agents()), dockerContainers, settings, null);
        serverPingRequestExecutor.execute();

        assertFalse(dockerContainers.containsKey(container.id()));
    }
}

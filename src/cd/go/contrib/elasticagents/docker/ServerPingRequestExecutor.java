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

import com.spotify.docker.client.ContainerNotFoundException;
import com.spotify.docker.client.DockerException;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.joda.time.Period;

import java.util.Collection;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.*;

public class ServerPingRequestExecutor implements RequestExecutor {

    private final DockerContainers containers;
    private final GoApplicationAccessor accessor;
    private final Agents agents;

    public ServerPingRequestExecutor(DockerContainers containers, ServerPingRequest serverPingRequest, GoApplicationAccessor accessor) {
        this.containers = containers;
        this.accessor = accessor;
        this.agents = serverPingRequest.agents();
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        for (String containerId : agents.keySet()) {
            try {
                containers.refresh(containerId);
            } catch (ContainerNotFoundException e) {
                LOG.warn("Was expecting a container with id " + containerId + " but it was missing!");
            }
        }

        disableIdleAgents();
        terminateDisabledAgents();

        terminateInstancesThatNeverRegistered();
        return DefaultGoPluginApiResponse.success("");
    }

    private void disableIdleAgents() {
        Collection<Agent> toBeDisabled = agents.findInstancesToDisable();

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DISABLE_AGENT, API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(Agent.toJSONArray(toBeDisabled));
        GoApiResponse response = this.accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("The server sent an unexpected status code " + response.responseCode() + " with the response body " + response.responseBody());
        }
    }

    private void terminateDisabledAgents() throws DockerException, InterruptedException {
        Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();
        for (Agent agent : toBeDeleted) {
            containers.terminate(agent.elasticAgentId());
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DELETE_AGENT, API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(Agent.toJSONArray(toBeDeleted));
        GoApiResponse response = this.accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("The server sent an unexpected status code " + response.responseCode() + " with the response body " + response.responseBody());
        }
    }

    private void terminateInstancesThatNeverRegistered() throws DockerException, InterruptedException {
        DockerContainers toTerminate = containers.unregisteredForMoreThan(new Period().withMinutes(1));

        if (toTerminate.isEmpty()) {
            return;
        }

        LOG.warn("Terminating instances that did not register " + toTerminate.keySet() + ".");
        toTerminate.terminateAll();
    }

}

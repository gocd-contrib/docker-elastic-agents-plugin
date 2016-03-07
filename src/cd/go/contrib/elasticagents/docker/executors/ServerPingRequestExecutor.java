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
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.PluginSettings;
import cd.go.contrib.elasticagents.docker.RequestExecutor;
import cd.go.contrib.elasticagents.docker.requests.ServerPingRequest;
import com.spotify.docker.client.ContainerNotFoundException;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collection;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class ServerPingRequestExecutor implements RequestExecutor {

    private final ServerPingRequest request;
    private final DockerContainers containers;
    private final PluginRequest pluginRequest;
    private final PluginSettings settings;

    public ServerPingRequestExecutor(ServerPingRequest request, DockerContainers containers, PluginRequest pluginRequest) {
        this.request = request;
        this.containers = containers;
        this.pluginRequest = pluginRequest;
        this.settings = pluginRequest.getConfiguration();
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        Agents agents = pluginRequest.getAgents();
        for (String containerId : agents.keySet()) {
            try {
                containers.refresh(containerId, settings);
            } catch (ContainerNotFoundException e) {
                LOG.warn("Was expecting a container with id " + containerId + " but it was missing!");
            }
        }

        disableIdleAgents(agents);

        agents = pluginRequest.getAgents();
        terminateDisabledAgents(agents);
        containers.terminateUnregisteredInstances(settings, agents);

        return DefaultGoPluginApiResponse.success("");
    }

    private void disableIdleAgents(Agents agents) {
        this.pluginRequest.disable(agents.findInstancesToDisable());
    }

    private void terminateDisabledAgents(Agents agents) throws Exception {
        Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();

        for (Agent agent : toBeDeleted) {
            containers.terminate(agent.elasticAgentId(), settings);
        }

        this.pluginRequest.delete(toBeDeleted);
    }

}

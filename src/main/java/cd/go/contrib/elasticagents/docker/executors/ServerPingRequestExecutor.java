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

import cd.go.contrib.elasticagents.docker.*;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collection;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class ServerPingRequestExecutor implements RequestExecutor {

    private final AgentInstances agentInstances;
    private final PluginRequest pluginRequest;

    public ServerPingRequestExecutor(AgentInstances agentInstances, PluginRequest pluginRequest) {
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        PluginSettings pluginSettings = pluginRequest.getPluginSettings();

        Agents allAgents = pluginRequest.listAgents();
        Agents missingAgents = new Agents();

        for (Agent agent : allAgents.agents()) {
            if (agentInstances.find(agent.elasticAgentId()) == null) {
                LOG.warn("Was expecting a container with name " + agent.elasticAgentId() + ", but it was missing!");
                missingAgents.add(agent);
            }
        }

        Agents agentsToDisable = agentInstances.instancesCreatedAfterTimeout(pluginSettings, allAgents);
        agentsToDisable.addAll(missingAgents);

        disableIdleAgents(agentsToDisable);

        allAgents = pluginRequest.listAgents();
        terminateDisabledAgents(allAgents, pluginSettings);

        agentInstances.terminateUnregisteredInstances(pluginSettings, allAgents);

        return DefaultGoPluginApiResponse.success("");
    }

    private void disableIdleAgents(Agents agents) throws ServerRequestFailedException {
        pluginRequest.disableAgents(agents.findInstancesToDisable());
    }

    private void terminateDisabledAgents(Agents agents, PluginSettings pluginSettings) throws Exception {
        Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();

        for (Agent agent : toBeDeleted) {
            agentInstances.terminate(agent.elasticAgentId(), pluginSettings);
        }

        pluginRequest.deleteAgents(toBeDeleted);
    }

}

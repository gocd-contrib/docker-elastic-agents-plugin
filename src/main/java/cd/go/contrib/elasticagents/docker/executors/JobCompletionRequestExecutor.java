/*
 * Copyright 2018 ThoughtWorks, Inc.
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
import cd.go.contrib.elasticagents.docker.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.List;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class JobCompletionRequestExecutor implements RequestExecutor {
    private final JobCompletionRequest jobCompletionRequest;
    private final AgentInstances<DockerContainer> agentInstances;
    private final PluginRequest pluginRequest;

    public JobCompletionRequestExecutor(JobCompletionRequest jobCompletionRequest, AgentInstances<DockerContainer> agentInstances, PluginRequest pluginRequest) {
        this.jobCompletionRequest = jobCompletionRequest;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        PluginSettings pluginSettings = pluginRequest.getPluginSettings();
        String elasticAgentId = jobCompletionRequest.getElasticAgentId();
        Agent agent = new Agent(elasticAgentId);
        LOG.debug("[Job Completion] Terminating elastic agent with id {} on job completion {}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier());
        List<Agent> agents = Arrays.asList(agent);
        pluginRequest.disableAgents(agents);
        agentInstances.terminate(agent.elasticAgentId(), pluginSettings);
        pluginRequest.deleteAgents(agents);
        return DefaultGoPluginApiResponse.success("");
    }
}

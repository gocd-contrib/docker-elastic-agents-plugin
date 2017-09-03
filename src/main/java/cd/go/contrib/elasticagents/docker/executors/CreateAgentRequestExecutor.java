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

import cd.go.contrib.elasticagents.docker.AgentInstances;
import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.RequestExecutor;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public class CreateAgentRequestExecutor implements RequestExecutor {
    private final AgentInstances<DockerContainer> agentInstances;
    private final PluginRequest pluginRequest;
    private final CreateAgentRequest request;

    public CreateAgentRequestExecutor(CreateAgentRequest request, AgentInstances<DockerContainer> agentInstances, PluginRequest pluginRequest) {
        this.request = request;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        agentInstances.create(request, pluginRequest.getPluginSettings());
        return new DefaultGoPluginApiResponse(200);
    }

}

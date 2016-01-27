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

package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.Agent;
import cd.go.contrib.elasticagents.Agents;
import cd.go.contrib.elasticagents.docker.*;
import cd.go.contrib.elasticagents.docker.executors.ServerPingRequestExecutor;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;

public class ServerPingRequest {
    private Agents agents;

    public ServerPingRequest() {

    }

    public ServerPingRequest(Agents agents) {
        this.agents = agents;
    }

    public Agents agents() {
        return agents;
    }

    public static ServerPingRequest fromJSON(String json) {
        return new ServerPingRequest(new Agents(Agent.fromJSONArray(json)));
    }

    public RequestExecutor executor(DockerContainers containers, PluginSettings settings, GoApplicationAccessor accessor) {
        return new ServerPingRequestExecutor(this, containers, settings, accessor);
    }
}

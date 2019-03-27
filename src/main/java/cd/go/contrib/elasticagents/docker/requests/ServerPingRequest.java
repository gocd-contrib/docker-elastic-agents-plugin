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

package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.docker.ClusterProfile;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.executors.ServerPingRequestExecutor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerPingRequest {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private List<ClusterProfile> allClusterProfileProperties;

    public ServerPingRequest() {
    }

    public ServerPingRequest(List<Map<String, String>> allClusterProfileProperties) {
        this.allClusterProfileProperties = allClusterProfileProperties.stream()
                .map(clusterProfile -> ClusterProfile.fromConfiguration(clusterProfile))
                .collect(Collectors.toList());
    }

    public List<ClusterProfile> allClusterProfileProperties() {
        return allClusterProfileProperties;
    }

    public static ServerPingRequest fromJSON(String json) {
        return GSON.fromJson(json, ServerPingRequest.class);
    }

    @Override
    public String toString() {
        return "ServerPingRequest{" +
                "allClusterProfileProperties=" + allClusterProfileProperties +
                '}';
    }

    public ServerPingRequestExecutor executor(Map<String, DockerContainers> clusterSpecificAgentInstances, PluginRequest pluginRequest) {
        return new ServerPingRequestExecutor(this, clusterSpecificAgentInstances, pluginRequest);
    }
}

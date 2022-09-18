/*
 * Copyright 2022 Thoughtworks, Inc.
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

import cd.go.contrib.elasticagents.docker.executors.*;
import cd.go.contrib.elasticagents.docker.requests.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.docker.Constants.PLUGIN_IDENTIFIER;

@Extension
public class DockerPlugin implements GoPlugin {

    public static final Logger LOG = Logger.getLoggerFor(DockerPlugin.class);

    private Map<String, DockerContainers> clusterSpecificAgentInstances;
    private PluginRequest pluginRequest;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        pluginRequest = new PluginRequest(accessor);
        clusterSpecificAgentInstances = new HashMap<>();
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        ClusterProfileProperties clusterProfileProperties;
        try {
            switch (Request.fromString(request.requestName())) {
                case REQUEST_SHOULD_ASSIGN_WORK:
                    ShouldAssignWorkRequest shouldAssignWorkRequest = ShouldAssignWorkRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = shouldAssignWorkRequest.getClusterProfileProperties();
                    refreshInstancesForCluster(clusterProfileProperties);
                    return shouldAssignWorkRequest.executor(getAgentInstancesFor(clusterProfileProperties)).execute();
                case REQUEST_CREATE_AGENT:
                    CreateAgentRequest createAgentRequest = CreateAgentRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = createAgentRequest.getClusterProfileProperties();
                    refreshInstancesForCluster(clusterProfileProperties);
                    return createAgentRequest.executor(getAgentInstancesFor(clusterProfileProperties), pluginRequest).execute();
                case REQUEST_SERVER_PING:
                    ServerPingRequest serverPingRequest = ServerPingRequest.fromJSON(request.requestBody());
                    List<ClusterProfileProperties> listOfClusterProfileProperties = serverPingRequest.allClusterProfileProperties();
                    refreshInstancesForAllClusters(listOfClusterProfileProperties);
                    return serverPingRequest.executor(clusterSpecificAgentInstances, pluginRequest).execute();
                case REQUEST_GET_ELASTIC_AGENT_PROFILE_METADATA:
                    return new GetProfileMetadataExecutor().execute();
                case REQUEST_GET_ELASTIC_AGENT_PROFILE_VIEW:
                    return new GetProfileViewExecutor().execute();
                case REQUEST_VALIDATE_ELASTIC_AGENT_PROFILE:
                    return ProfileValidateRequest.fromJSON(request.requestBody()).executor().execute();
                case REQUEST_GET_ICON:
                    return new GetPluginSettingsIconExecutor().execute();
                case REQUEST_GET_CLUSTER_PROFILE_METADATA:
                    return new GetClusterProfileMetadataExecutor().execute();
                case REQUEST_VALIDATE_CLUSTER_PROFILE_CONFIGURATION:
                    return ClusterProfileValidateRequest.fromJSON(request.requestBody()).executor().execute();
                case REQUEST_GET_CLUSTER_PROFILE_VIEW:
                    return new GetClusterProfileViewRequestExecutor().execute();
                case REQUEST_MIGRATE_CONFIGURATION:
                    return MigrateConfigurationRequest.fromJSON(request.requestBody()).executor().execute();
                case REQUEST_AGENT_STATUS_REPORT:
                    AgentStatusReportRequest statusReportRequest = AgentStatusReportRequest.fromJSON(request.requestBody());
                    ClusterProfileProperties clusterProfile = statusReportRequest.getClusterProfile();
                    refreshInstancesForCluster(clusterProfile);
                    return statusReportRequest.executor(pluginRequest, clusterSpecificAgentInstances.get(clusterProfile.uuid())).execute();
                case REQUEST_CLUSTER_STATUS_REPORT:
                    ClusterStatusReportRequest clusterStatusReportRequest = ClusterStatusReportRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = clusterStatusReportRequest.getClusterProfile();
                    refreshInstancesForCluster(clusterProfileProperties);
                    return clusterStatusReportRequest.executor(clusterSpecificAgentInstances.get(clusterProfileProperties.uuid())).execute();
                case REQUEST_CAPABILITIES:
                    return new GetCapabilitiesExecutor().execute();
                case REQUEST_JOB_COMPLETION:
                    JobCompletionRequest jobCompletionRequest = JobCompletionRequest.fromJSON(request.requestBody());
                    clusterProfileProperties = jobCompletionRequest.getClusterProfileProperties();
                    refreshInstancesForCluster(clusterProfileProperties);
                    return jobCompletionRequest.executor(getAgentInstancesFor(clusterProfileProperties), pluginRequest).execute();
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshInstancesForAllClusters(List<ClusterProfileProperties> listOfClusterProfileProperties) throws Exception {
        for (ClusterProfileProperties clusterProfileProperties : listOfClusterProfileProperties) {
            refreshInstancesForCluster(clusterProfileProperties);
        }
    }

    private AgentInstances<DockerContainer> getAgentInstancesFor(ClusterProfileProperties clusterProfileProperties) {
        return clusterSpecificAgentInstances.get(clusterProfileProperties.uuid());
    }

    private void refreshInstancesForCluster(ClusterProfileProperties clusterProfileProperties) throws Exception {
        DockerContainers dockerContainers = clusterSpecificAgentInstances.getOrDefault(clusterProfileProperties.uuid(), new DockerContainers());
        dockerContainers.refreshAll(clusterProfileProperties);

        clusterSpecificAgentInstances.put(clusterProfileProperties.uuid(), dockerContainers);
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }

}

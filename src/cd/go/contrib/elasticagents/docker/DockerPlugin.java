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

import cd.go.contrib.elasticagents.docker.executors.GetPluginConfigurationExecutor;
import cd.go.contrib.elasticagents.docker.executors.GetViewRequestExecutor;
import cd.go.contrib.elasticagents.docker.requests.*;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;

@Extension
public class DockerPlugin implements GoPlugin {
    public static final String API_VERSION = "1.0";
    public static final GoPluginIdentifier PLUGIN_IDENTIFIER = new GoPluginIdentifier(Constants.EXTENSION_NAME, Arrays.asList(API_VERSION));

    public static final Logger LOG = Logger.getLoggerFor(DockerPlugin.class);

    private GoApplicationAccessor accessor;
    private DockerContainers containers;

    @Load
    public void onLoad(PluginContext ctx) {
        containers = new DockerContainers(new PluginSettingsRequest(this));
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.accessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) throws UnhandledRequestTypeException {
        try {
            switch (goPluginApiRequest.requestName()) {
                case Constants.REQUEST_CAN_PLUGIN_HANDLE:
                    return CanPluginHandleRequest.fromJSON(goPluginApiRequest.requestBody()).executor(containers).execute();
                case Constants.REQUEST_SHOULD_ASSIGN_WORK:
                    return ShouldAssignWorkRequest.fromJSON(goPluginApiRequest.requestBody()).executor(containers).execute();
                case Constants.REQUEST_CREATE_AGENT:
                    return CreateAgentRequest.fromJSON(goPluginApiRequest.requestBody()).executor(containers).execute();
                case Constants.REQUEST_SERVER_PING:
                    return ServerPingRequest.fromJSON(goPluginApiRequest.requestBody()).executor(containers, accessor).execute();
                case Constants.PLUGIN_SETTINGS_GET_VIEW:
                    return new GetViewRequestExecutor().execute();
                case Constants.PLUGIN_SETTINGS_GET_CONFIGURATION:
                    return new GetPluginConfigurationExecutor().execute();
                case Constants.PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    return ValidatePluginSettings.fromJSON(goPluginApiRequest.requestBody()).executor().execute();
                case Constants.REQUEST_NOTIFY_AGENT_BUSY:
                    return DefaultGoPluginApiResponse.success("");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new UnhandledRequestTypeException(goPluginApiRequest.requestName());
    }


    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }

    public static void main(String[] args) throws Exception {
//        DockerPlugin dockerPlugin = new DockerPlugin();
//        dockerPlugin.onLoad(null);
//        new CreateAgentRequest("secret", Arrays.asList("foo", "bar"), "pre-prod").executor(dockerPlugin.containers, null).execute();
    }

    public GoApiResponse submit(DefaultGoApiRequest request) {
        return accessor.submit(request);
    }
}

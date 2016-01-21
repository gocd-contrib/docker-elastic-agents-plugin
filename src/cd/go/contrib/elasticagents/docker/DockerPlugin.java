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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;

@Extension
public class DockerPlugin implements GoPlugin {
    public static final String API_VERSION = "1.0";
    public static final GoPluginIdentifier PLUGIN_IDENTIFIER = new GoPluginIdentifier(Constants.EXTENSION_NAME, Arrays.asList(API_VERSION));

    public static final Logger LOG = Logger.getLoggerFor(DockerPlugin.class);

    private DockerContainers containers;
    private GoApplicationAccessor accessor;

    @Load
    public void onLoad(PluginContext ctx) {
        try {
            DefaultDockerClient docker = DefaultDockerClient.builder()
                    .uri(URI.create("https://172.16.37.139:2376"))
                    .dockerCertificates(new DockerCertificates(Paths.get("/Users/ketanpadegaonkar/.docker/machine/machines/default")))
                    .build();
            containers = new DockerContainers(docker);
        } catch (DockerCertificateException e) {
            throw new RuntimeException(e);
        }
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
        DockerPlugin dockerPlugin = new DockerPlugin();
        dockerPlugin.onLoad(null);
        new CreateAgentRequest("secret", Arrays.asList("foo", "bar"), "pre-prod").executor(dockerPlugin.containers).execute();
    }
}

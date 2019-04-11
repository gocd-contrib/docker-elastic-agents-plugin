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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.RequestExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

public class GetClusterProfileMetadataExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Metadata GO_SERVER_URL = new GoServerURLMetadata();
    public static final Metadata ENVIRONMENT_VARIABLES = new Metadata("EnvironmentVariables", false, false);
    public static final Metadata MAX_DOCKER_CONTAINERS = new Metadata("MaxDockerContainers", true, false);
    public static final Metadata DOCKER_URI = new Metadata("DockerUri", true, false);
    public static final Metadata AUTO_REGISTER_TIMEOUT = new Metadata("AutoRegisterTimeout", true, false);
    public static final Metadata DOCKER_CA_CERT = new Metadata("DockerCaCert", false, false);
    public static final Metadata DOCKER_CLIENT_KEY = new Metadata("DockerClientKey", false, false);
    public static final Metadata DOCKER_CLIENT_CERT = new Metadata("DockerClientCert", false, false);
    public static final Metadata ENABLE_PRIVATE_REGISTRY_AUTHENTICATION = new Metadata("EnablePrivateRegistryAuthentication", false, false);
    public static final Metadata PRIVATE_REGISTRY_SERVER = new Metadata("PrivateRegistryServer", false, false);
    public static final Metadata PRIVATE_REGISTRY_CUSTOM_CREDENTIALS = new Metadata("PrivateRegistryCustomCredentials", false, false);
    public static final Metadata PRIVATE_REGISTRY_USERNAME = new Metadata("PrivateRegistryUsername", false, false);
    public static final Metadata PRIVATE_REGISTRY_PASSWORD = new Metadata("PrivateRegistryPassword", false, true);
    public static final Metadata PULL_ON_CONTAINER_CREATE = new Metadata("PullOnContainerCreate", false, false);

    public static final List<Metadata> FIELDS = new ArrayList<>();

    static {
        FIELDS.add(GO_SERVER_URL);
        FIELDS.add(ENVIRONMENT_VARIABLES);
        FIELDS.add(MAX_DOCKER_CONTAINERS);
        FIELDS.add(DOCKER_URI);
        FIELDS.add(AUTO_REGISTER_TIMEOUT);
        FIELDS.add(DOCKER_CA_CERT);
        FIELDS.add(DOCKER_CLIENT_KEY);
        FIELDS.add(DOCKER_CLIENT_CERT);
        FIELDS.add(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION);
        FIELDS.add(PRIVATE_REGISTRY_SERVER);
        FIELDS.add(PRIVATE_REGISTRY_CUSTOM_CREDENTIALS);
        FIELDS.add(PRIVATE_REGISTRY_USERNAME);
        FIELDS.add(PRIVATE_REGISTRY_PASSWORD);
        FIELDS.add(PULL_ON_CONTAINER_CREATE);
    }

    @Override

    public GoPluginApiResponse execute() throws Exception {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }
}

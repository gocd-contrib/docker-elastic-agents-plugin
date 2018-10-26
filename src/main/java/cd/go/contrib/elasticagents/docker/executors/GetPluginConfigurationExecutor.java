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

import cd.go.contrib.elasticagents.docker.RequestExecutor;
import cd.go.contrib.elasticagents.docker.requests.ValidatePluginSettings;
import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetPluginConfigurationExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Field GO_SERVER_URL = new GoServerURLField("go_server_url", "Go Server URL", null, true, false, "0");
    public static final Field ENVIRONMENT_VARIABLES = new Field("environment_variables", "Environment Variables", null, false, false, "1");
    public static final Field MAX_DOCKER_CONTAINERS = new PositiveNumberField("max_docker_containers", "Maximum containers to allow", null, true, false, "2");
    public static final Field DOCKER_URL = new NonBlankField("docker_uri", "Docker URI", null, true, false, "3");
    public static final Field AUTOREGISTER_TIMEOUT = new PositiveNumberField("auto_register_timeout", "Agent auto-register Timeout (in minutes)", "10", true, false, "4");
    public static final Field DOCKER_CA_CERT = new Field("docker_ca_cert", "Docker CA Certificate", null, false, true, "5");
    public static final Field DOCKER_CLIENT_KEY = new Field("docker_client_key", "Docker Client Key", null, false, true, "6");
    public static final Field DOCKER_CLIENT_CERT = new Field("docker_client_cert", "Docker Client Certificate", null, false, true, "7");
    public static final Field ENABLE_PRIVATE_REGISTRY_AUTHENTICATION = new Field("enable_private_registry_authentication", "Use Private Registry", "false", true, false, "8");
    private static final Predicate<ValidatePluginSettings> privateRegistryFieldsPredicate = new Predicate<ValidatePluginSettings>() {
        @Override public boolean apply(ValidatePluginSettings settings) { return Boolean.parseBoolean(settings.get(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION.key()));}
    };
    public static final Field PRIVATE_REGISTRY_SERVER = new ConditionalNonBlankField("private_registry_server", "Private Registry Server", null, false, false, "9", privateRegistryFieldsPredicate);
    public static final Field PRIVATE_REGISTRY_USERNAME = new ConditionalNonBlankField("private_registry_username", "Private Registry Username", null, false, false, "10", privateRegistryFieldsPredicate);
    public static final Field PRIVATE_REGISTRY_PASSWORD = new ConditionalNonBlankField("private_registry_password", "Private Registry Password", null, false, true, "11", privateRegistryFieldsPredicate);
    public static final Field PULL_ON_CONTAINER_CREATE = new Field("pull_on_container_create", "Pull image before creating the container", "false", true, false, "12");

    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put(GO_SERVER_URL.key(), GO_SERVER_URL);
        FIELDS.put(ENVIRONMENT_VARIABLES.key(), ENVIRONMENT_VARIABLES);
        FIELDS.put(MAX_DOCKER_CONTAINERS.key(), MAX_DOCKER_CONTAINERS);
        FIELDS.put(DOCKER_URL.key(), DOCKER_URL);
        FIELDS.put(AUTOREGISTER_TIMEOUT.key(), AUTOREGISTER_TIMEOUT);

        // certs
        FIELDS.put(DOCKER_CA_CERT.key(), DOCKER_CA_CERT);
        FIELDS.put(DOCKER_CLIENT_KEY.key(), DOCKER_CLIENT_KEY);
        FIELDS.put(DOCKER_CLIENT_CERT.key(), DOCKER_CLIENT_CERT);

        FIELDS.put(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION.key(), ENABLE_PRIVATE_REGISTRY_AUTHENTICATION);
        FIELDS.put(PRIVATE_REGISTRY_SERVER.key(), PRIVATE_REGISTRY_SERVER);
        FIELDS.put(PRIVATE_REGISTRY_USERNAME.key(), PRIVATE_REGISTRY_USERNAME);
        FIELDS.put(PRIVATE_REGISTRY_PASSWORD.key(), PRIVATE_REGISTRY_PASSWORD);

        FIELDS.put(PULL_ON_CONTAINER_CREATE.key(), PULL_ON_CONTAINER_CREATE);
    }

    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }

}

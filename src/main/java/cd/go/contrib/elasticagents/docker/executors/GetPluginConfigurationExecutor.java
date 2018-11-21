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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.docker.executors.Field.next;

public class GetPluginConfigurationExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final String GO_SERVER_URL = "go_server_url";
    public static final String ENVIRONMENT_VARIABLES = "environment_variables";
    public static final String MAX_DOCKER_CONTAINERS = "max_docker_containers";
    public static final String DOCKER_URI = "docker_uri";
    public static final String AUTO_REGISTER_TIMEOUT = "auto_register_timeout";
    public static final String DOCKER_CA_CERT = "docker_ca_cert";
    public static final String DOCKER_CLIENT_KEY = "docker_client_key";
    public static final String DOCKER_CLIENT_CERT = "docker_client_cert";
    public static final String ENABLE_PRIVATE_REGISTRY_AUTHENTICATION = "enable_private_registry_authentication";
    public static final String PRIVATE_REGISTRY_SERVER = "private_registry_server";
    public static final String PRIVATE_REGISTRY_CUSTOM_CREDENTIALS = "private_registry_custom_credentials";
    public static final String PRIVATE_REGISTRY_USERNAME = "private_registry_username";
    public static final String PRIVATE_REGISTRY_PASSWORD = "private_registry_password";
    public static final String PULL_ON_CONTAINER_CREATE = "pull_on_container_create";

    private static final Predicate<ValidatePluginSettings> privateRegistryFieldsPredicate = new Predicate<ValidatePluginSettings>() {
        @Override
        public boolean apply(ValidatePluginSettings settings) {
            return Boolean.parseBoolean(settings.get(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION));
        }
    };
    private static final Predicate<ValidatePluginSettings> privateRegistryCredentialsPredicate = new Predicate<ValidatePluginSettings>() {
        @Override
        public boolean apply(ValidatePluginSettings settings) {
            return privateRegistryFieldsPredicate.apply(settings) && Boolean.parseBoolean(settings.get(PRIVATE_REGISTRY_CUSTOM_CREDENTIALS));
        }
    };

    private static final List<Field> FIELD_LIST = Arrays.asList(
            new GoServerURLField(next()),
            new Field(ENVIRONMENT_VARIABLES, "Environment Variables", null, false, false, next()),
            new PositiveNumberField(MAX_DOCKER_CONTAINERS, "Maximum containers to allow", null, true, false, next()),
            new NonBlankField(DOCKER_URI, "Docker URI", null, false, next()),
            new PositiveNumberField(AUTO_REGISTER_TIMEOUT, "Agent auto-register Timeout (in minutes)", "10", true, false, next()),
            new Field(DOCKER_CA_CERT, "Docker CA Certificate", null, false, true, next()),
            new Field(DOCKER_CLIENT_KEY, "Docker Client Key", null, false, true, next()),
            new Field(DOCKER_CLIENT_CERT, "Docker Client Certificate", null, false, true, next()),
            new Field(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION, "Use Private Registry", "false", false, false, next()),
            new ConditionalNonBlankField(PRIVATE_REGISTRY_SERVER, "Private Registry Server", null, false, next(), privateRegistryFieldsPredicate),
            new Field(PRIVATE_REGISTRY_CUSTOM_CREDENTIALS, "Private Registry credentials setup", "true", true, false, next()),
            new ConditionalNonBlankField(PRIVATE_REGISTRY_USERNAME, "Private Registry Username", null, false, next(), privateRegistryCredentialsPredicate),
            new ConditionalNonBlankField(PRIVATE_REGISTRY_PASSWORD, "Private Registry Password", null, true, next(), privateRegistryCredentialsPredicate),
            new Field(PULL_ON_CONTAINER_CREATE, "Pull image before creating the container", "false", true, false, next())
    );

    public static final Map<String, Field> FIELDS = FIELD_LIST.stream()
            .collect(Collectors.toMap(Field::key, Function.identity()));

    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }

}

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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.RequestExecutor;
import cd.go.contrib.elasticagents.docker.requests.ClusterProfileValidateRequest;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static cd.go.contrib.elasticagents.docker.executors.GetClusterProfileMetadataExecutor.FIELDS;


public class ClusterProfileValidateRequestExecutor implements RequestExecutor {
    private final ClusterProfileValidateRequest request;
    private static final Gson GSON = new Gson();

    public ClusterProfileValidateRequestExecutor(ClusterProfileValidateRequest request) {
        this.request = request;
    }

    @Override
    public GoPluginApiResponse execute() {
        List<Map<String, String>> result = new ArrayList<>();
        List<String> knownFields = new ArrayList<>();

        for (Metadata field : FIELDS) {
            knownFields.add(field.getKey());
            Map<String, String> validationError = field.validate(getRequestProperty(field.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                result.add(Map.of("key", key, "message", "Is an unknown property"));
            }
        }

        result.addAll(validateNoDanglingPrivateRegistryCredentials());

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private List<Map<String, String>> validateNoDanglingPrivateRegistryCredentials() {
        List<Map<String, String>> result = new ArrayList<>();

        String enablePrivateRegistryAuthentication = getRequestProperty("enable_private_registry_authentication");
        String privateRegistryCustomCredentials = getRequestProperty("private_registry_custom_credentials");
        boolean usePrivateRegistry = "true".equals(enablePrivateRegistryAuthentication);
        boolean useCustomRegistryCredentials = "true".equals(privateRegistryCustomCredentials);
        boolean privateRegistryUsernameIsPresent = !StringUtils.isBlank(getRequestProperty("private_registry_username"));
        boolean privateRegistryPasswordIsPresent = !StringUtils.isBlank(getRequestProperty("private_registry_password"));

        if (usePrivateRegistry && !useCustomRegistryCredentials) {
            if (privateRegistryPasswordIsPresent || privateRegistryUsernameIsPresent) {
                result.add(Map.of(
                        "key", "enable_private_registry_authentication",
                        "message", "Please clear your private registry credentials before switching from custom credentials to using the docker configuration file."
                ));
            }

            if (privateRegistryUsernameIsPresent) {
                result.add(Map.of(
                        "key", "private_registry_username",
                        "message", "Please clear your private registry username before switching from custom credentials to using the docker configuration file."
                ));
            }

            if (privateRegistryPasswordIsPresent) {
                result.add(Map.of(
                        "key", "private_registry_password",
                        "message", "Please clear your private registry password before switching from custom credentials to using the docker configuration file."));
            }
        }

        return result;
    }

    private String getRequestProperty(String key) {
        return request.getProperties().get(key);
    }

}

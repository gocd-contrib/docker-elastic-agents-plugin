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

import java.util.*;
import org.apache.commons.lang.StringUtils;

import static cd.go.contrib.elasticagents.docker.executors.GetClusterProfileMetadataExecutor.FIELDS;


public class ClusterProfileValidateRequestExecutor implements RequestExecutor {
    private final ClusterProfileValidateRequest request;
    private static final Gson GSON = new Gson();

    public ClusterProfileValidateRequestExecutor(ClusterProfileValidateRequest request) {
        this.request = request;
    }

    private String getRequestProperty(String key) {
        return request.getProperties().get(key);
    }

    @Override
    public GoPluginApiResponse execute() {
        ArrayList<Map<String, String>> result = new ArrayList<>();

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
                LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
                validationError.put("key", key);
                validationError.put("message", "Is an unknown property");
                result.add(validationError);
            }
        }

        result.addAll(validateNoDanglingPrivateRegistryCredentials());

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

    private ArrayList<Map<String, String>> validateNoDanglingPrivateRegistryCredentials() {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        String enablePrivateRegistryAuthentication = getRequestProperty("enable_private_registry_authentication");
        String privateRegistryCustomCredentials = getRequestProperty("private_registry_custom_credentials");
        boolean usePrivateRegistry = enablePrivateRegistryAuthentication == null ? false : enablePrivateRegistryAuthentication.equals("true");
        boolean useCustomRegistryCredentials = privateRegistryCustomCredentials == null ? false : privateRegistryCustomCredentials.equals("true");
        boolean privateRegistryUsernameIsPresent = !StringUtils.isBlank(getRequestProperty("private_registry_username"));
        boolean privateRegistryPasswordIsPresent = !StringUtils.isBlank(getRequestProperty("private_registry_password"));

        if (usePrivateRegistry && !useCustomRegistryCredentials) {
            if (privateRegistryPasswordIsPresent || privateRegistryUsernameIsPresent) {
                HashMap<String, String> validationError = new HashMap<>();
                validationError.put("key", "enable_private_registry_authentication");
                validationError.put("message", "Please clear your private registry credentials before switching from custom credentials to using the docker configuration file.");
                result.add(validationError);
            }

            if (privateRegistryUsernameIsPresent) {
                HashMap<String, String> validationError = new HashMap<>();
                validationError.put("key", "private_registry_username");
                validationError.put("message", "Please clear your private registry username before switching from custom credentials to using the docker configuration file.");
                result.add(validationError);
            }

            if (privateRegistryPasswordIsPresent) {
                HashMap<String, String> validationError = new HashMap<>();
                validationError.put("key", "private_registry_password");
                validationError.put("message", "Please clear your private registry password before switching from custom credentials to using the docker configuration file.");
                result.add(validationError);
            }
        }

        return result;
    }
}

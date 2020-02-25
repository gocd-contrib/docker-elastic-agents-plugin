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
import cd.go.contrib.elasticagents.docker.requests.ProfileValidateRequest;
import cd.go.contrib.elasticagents.docker.validator.MemorySettingsProfileValidator;
import cd.go.contrib.elasticagents.docker.validator.ProfileValidator;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

import static cd.go.contrib.elasticagents.docker.executors.GetProfileMetadataExecutor.FIELDS;

public class ProfileValidateRequestExecutor implements RequestExecutor {
    private final ProfileValidateRequest request;
    private final List<ProfileValidator> validators = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public ProfileValidateRequestExecutor(ProfileValidateRequest request) {
        this.request = request;
        validators.add(new MemorySettingsProfileValidator());
    }

    @Override
    public GoPluginApiResponse execute() {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        List<String> knownFields = new ArrayList<>();

        for (Metadata field : FIELDS) {
            knownFields.add(field.getKey());

            Map<String, String> validationError = field.validate(request.getProperties().get(field.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        for (ProfileValidator validator : validators) {
            Map<String, String> validationError = validator.validate(request.getProperties());
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

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }
}

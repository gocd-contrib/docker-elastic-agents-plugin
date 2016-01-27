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
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.Map;

public class ValidateConfigurationExecutor implements RequestExecutor {

    private final ValidatePluginSettings settings;

    public ValidateConfigurationExecutor(ValidatePluginSettings settings) {
        this.settings = settings;
    }

    public GoPluginApiResponse execute() {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            Map<String, String> validationError = GetPluginConfigurationExecutor.FIELDS.get(entry.getKey()).validate(entry.getValue());
            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        return DefaultGoPluginApiResponse.success(new Gson().toJson(result));
    }
}

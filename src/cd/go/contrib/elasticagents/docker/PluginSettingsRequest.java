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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import java.util.Collections;

public class PluginSettingsRequest {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private final DockerPlugin dockerPlugin;

    public PluginSettingsRequest(DockerPlugin dockerPlugin) {
        this.dockerPlugin = dockerPlugin;
    }

    public PluginSettings getConfiguration() {
        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_GET_PLUGIN_SETTINGS, DockerPlugin.API_VERSION, DockerPlugin.PLUGIN_IDENTIFIER);
        request.setRequestBody(GSON.toJson(Collections.singletonMap("plugin-id", Constants.PLUGIN_ID)));
        GoApiResponse response = dockerPlugin.submit(request);

        if (response.responseCode() != 200) {
            throw new RuntimeException("Could not fetch settings for plugin " + Constants.PLUGIN_ID);
        }

        return PluginSettings.fromJSON(response.responseBody());
    }

}

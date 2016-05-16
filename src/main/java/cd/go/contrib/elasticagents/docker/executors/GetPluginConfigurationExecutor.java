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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetPluginConfigurationExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Field GO_SERVER_URL = new NonBlankField("go_server_url", "Go Server URL", null, true, false, "0");
    public static final Field DOCKER_URL = new NonBlankField("docker_uri", "Docker URI", null, true, false, "1");
    public static final Field AUTOREGISTER_TIMEOUT = new PositiveNumberField("auto_register_timeout", "Agent auto-register Timeout (in minutes)", "10", true, false, "2");
    public static final Field DOCKER_CA_CERT = new NonBlankField("docker_ca_cert", "Docker CA Certificate", null, false, true, "3");
    public static final Field DOCKER_CLIENT_KEY = new NonBlankField("docker_client_key", "Docker Client Key", null, false, true, "4");
    public static final Field DOCKER_CLIENT_CERT = new NonBlankField("docker_client_cert", "Docker Client Certificate", null, false, true, "5");

    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put(GO_SERVER_URL.key(), GO_SERVER_URL);
        FIELDS.put(DOCKER_URL.key(), DOCKER_URL);
        FIELDS.put(AUTOREGISTER_TIMEOUT.key(), AUTOREGISTER_TIMEOUT);

        // certs
        FIELDS.put(DOCKER_CA_CERT.key(), DOCKER_CA_CERT);
        FIELDS.put(DOCKER_CLIENT_KEY.key(), DOCKER_CLIENT_KEY);
        FIELDS.put(DOCKER_CLIENT_CERT.key(), DOCKER_CLIENT_CERT);
    }

    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }

}

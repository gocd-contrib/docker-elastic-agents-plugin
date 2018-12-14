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

import cd.go.contrib.elasticagents.docker.HostMetadata;
import cd.go.contrib.elasticagents.docker.RequestExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

public class GetProfileMetadataExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Metadata IMAGE = new Metadata("Image", true, false);
    public static final Metadata COMMAND = new Metadata("Command", false, false);
    public static final Metadata ENVIRONMENT = new Metadata("Environment", false, false);
    public static final Metadata HOSTS = new HostMetadata();
    public static final Metadata PRIVILEGED = new Metadata("Privileged", false, false);

    public static final List<Metadata> FIELDS = new ArrayList<>();

    static {
        FIELDS.add(IMAGE);
        FIELDS.add(COMMAND);
        FIELDS.add(ENVIRONMENT);
        FIELDS.add(HOSTS);
        FIELDS.add(PRIVILEGED);
    }

    @Override

    public GoPluginApiResponse execute() throws Exception {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }
}

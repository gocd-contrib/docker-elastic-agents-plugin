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

import cd.go.contrib.elasticagents.docker.*;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.Date;

public class CreateAgentRequestExecutor implements RequestExecutor {
    private static final DateTimeFormatter MESSAGE_PREFIX_FORMATTER = DateTimeFormat.forPattern("'##|'HH:mm:ss.SSS '[go]'");
    private final AgentInstances<DockerContainer> agentInstances;
    private final PluginRequest pluginRequest;
    private final CreateAgentRequest request;

    public CreateAgentRequestExecutor(CreateAgentRequest request, AgentInstances<DockerContainer> agentInstances, PluginRequest pluginRequest) {
        this.request = request;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        ConsoleLogAppender consoleLogAppender = text -> {
            final String message = String.format("%s %s\n", LocalTime.now().toString(MESSAGE_PREFIX_FORMATTER), text);
            pluginRequest.appendToConsoleLog(request.jobIdentifier(), message);
        };

        consoleLogAppender.accept(String.format("Received request to create a container of %s", request.dockerImage()));

        try {
            agentInstances.create(request, pluginRequest, consoleLogAppender);
        } catch (Exception e) {
            consoleLogAppender.accept(String.format("Failed while creating container: %s", e.getMessage()));
            throw e;
        }

        return new DefaultGoPluginApiResponse(200);
    }

}

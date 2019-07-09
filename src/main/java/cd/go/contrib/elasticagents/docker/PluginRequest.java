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

import com.google.gson.Gson;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import java.util.*;

import static cd.go.contrib.elasticagents.docker.Constants.*;
import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

/**
 * Instances of this class know how to send messages to the GoCD Server.
 */
public class PluginRequest {
    private final GoApplicationAccessor accessor;

    public PluginRequest(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    public Agents listAgents() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_LIST_AGENTS, PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.listAgents(response);
        }

        return new Agents(Agent.fromJSONArray(response.responseBody()));
    }

    public void disableAgents(Collection<Agent> toBeDisabled) throws ServerRequestFailedException {
        if (toBeDisabled.isEmpty()) {
            return;
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DISABLE_AGENT, PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(Agent.toJSONArray(toBeDisabled));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.disableAgents(response);
        }
    }

    public void deleteAgents(Collection<Agent> toBeDeleted) throws ServerRequestFailedException {
        if (toBeDeleted.isEmpty()) {
            return;
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DELETE_AGENT, PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(Agent.toJSONArray(toBeDeleted));
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.deleteAgents(response);
        }
    }

    public void addServerHealthMessage(List<Map<String, String>> messages) {
        Gson gson = new Gson();

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_SERVER_HEALTH_ADD_MESSAGES, PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);

        request.setRequestBody(gson.toJson(messages));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("The server sent an unexpected status code " + response.responseCode() + " with the response body " + response.responseBody());
        }
    }

    public void appendToConsoleLog(JobIdentifier jobIdentifier, String text) throws ServerRequestFailedException {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("pipelineName", jobIdentifier.getPipelineName());
        requestMap.put("pipelineCounter", String.valueOf(jobIdentifier.getPipelineCounter()));
        requestMap.put("stageName", jobIdentifier.getStageName());
        requestMap.put("stageCounter", jobIdentifier.getStageCounter());
        requestMap.put("jobName", jobIdentifier.getJobName());
        requestMap.put("text", text);

        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_APPEND_TO_CONSOLE_LOG, CONSOLE_LOG_API_VERSION, PLUGIN_IDENTIFIER);
        request.setRequestBody(new GsonBuilder().create().toJson(requestMap));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("Failed to append to console log for " + jobIdentifier.represent() + " with text: " + text);
        }
    }
}

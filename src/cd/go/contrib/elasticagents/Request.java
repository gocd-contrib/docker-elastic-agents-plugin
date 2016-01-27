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

package cd.go.contrib.elasticagents;

public enum Request {
    // elastic agent related requests that the server makes to the plugin
    REQUEST_CREATE_AGENT(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".create-agent"),
    REQUEST_CAN_PLUGIN_HANDLE(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".can-plugin-handle"),
    REQUEST_SERVER_PING(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".server-ping"),
    REQUEST_SHOULD_ASSIGN_WORK(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".should-assign-work"),
    REQUEST_NOTIFY_AGENT_BUSY(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".notify-agent-busy"),
    REQUEST_NOTIFY_AGENT_IDLE(Constants.ELASTIC_AGENT_REQUEST_PREFIX + ".notify-agent-idle"),

    // settings related requests that the server makes to the plugin
    PLUGIN_SETTINGS_GET_CONFIGURATION(Constants.GO_PLUGIN_SETTINGS_PREFIX + ".get-configuration"),
    PLUGIN_SETTINGS_GET_VIEW(Constants.GO_PLUGIN_SETTINGS_PREFIX + ".get-view"),
    PLUGIN_SETTINGS_VALIDATE_CONFIGURATION(Constants.GO_PLUGIN_SETTINGS_PREFIX + ".validate-configuration"),;

    private final String requestName;

    Request(String requestName) {
        this.requestName = requestName;
    }

    public static Request fromString(String requestName) {
        if (requestName != null) {
            for (Request request : Request.values()) {
                if (requestName.equalsIgnoreCase(request.requestName)) {
                    return request;
                }
            }
        }

        return null;
    }

    private static class Constants {
        public static final String ELASTIC_AGENT_REQUEST_PREFIX = "go.cd.elastic-agent";
        public static final String GO_PLUGIN_SETTINGS_PREFIX = "go.plugin-settings";
    }
}

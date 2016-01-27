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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Agent {
    public static final Type AGENT_METADATA_LIST_TYPE = new TypeToken<ArrayList<Agent>>() {
    }.getType();
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private String agentId;
    private String agentState;
    private String buildState;
    private String configState;

    public Agent() {

    }

    public Agent(String agentId, String agentState, String buildState, String configState) {
        this.agentId = agentId;
        this.agentState = agentState;
        this.buildState = buildState;
        this.configState = configState;
    }

    public String elasticAgentId() {
        return agentId;
    }

    public String agentState() {
        return agentState;
    }

    public String buildState() {
        return buildState;
    }

    public String configState() {
        return configState;
    }

    public static List<Agent> fromJSONArray(String json) {
        return GSON.fromJson(json, AGENT_METADATA_LIST_TYPE);
    }

    public static String toJSONArray(Collection<Agent> metadata) {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().toJson(metadata);
    }

    @Override
    public String toString() {
        return "Agent{" +
                "agentId='" + agentId + '\'' +
                ", agentState='" + agentState + '\'' +
                ", buildState='" + buildState + '\'' +
                ", configState='" + configState + '\'' +
                '}';
    }
}

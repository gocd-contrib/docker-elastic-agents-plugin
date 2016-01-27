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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.HashMap;

public class Agents extends HashMap<String, Agent> {
    private static final Predicate<Agent> AGENT_IDLE_PREDICATE = new Predicate<Agent>() {
        @Override
        public boolean apply(Agent metadata) {
            String agentState = metadata.agentState();
            return metadata.configState().equals("Enabled") && (agentState.equals("Idle") || agentState.equals("Missing") || agentState.equals("LostContact"));
        }
    };

    private static final Predicate<Agent> AGENT_DISABLED_PREDICATE = new Predicate<Agent>() {
        @Override
        public boolean apply(Agent metadata) {
            String agentState = metadata.agentState();
            return metadata.configState().equals("Disabled") && (agentState.equals("Idle") || agentState.equals("Missing") || agentState.equals("LostContact"));
        }
    };

    public Agents() {

    }

    public Agents(Collection<Agent> agents) {
        for (Agent agent : agents) {
            put(agent.elasticAgentId(), agent);
        }
    }


    public Collection<Agent> findInstancesToDisable() {
        return Collections2.filter(values(), AGENT_IDLE_PREDICATE);
    }

    public Collection<Agent> findInstancesToTerminate() {
        return Collections2.filter(values(), AGENT_DISABLED_PREDICATE);
    }
}

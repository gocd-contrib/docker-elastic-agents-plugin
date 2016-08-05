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

import cd.go.contrib.elasticagents.docker.executors.AgentNotFoundException;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;

/**
 * Plugin implementors should implement these methods to interface to your cloud.
 * This interface is merely a suggestion for a very simple plugin. You may change it to your needs.
 */
public interface AgentInstances<T> {
    /**
     * This message is sent to request creation of an agent instance.
     * Implementations may, at their discretion choose to not spin up an agent instance.
     *
     * So that instances created are auto-registered with the server, the agent instance should have an
     * <code>autoregister.properties</code>.
     *
     * @param request   the request object
     * @param settings  the plugin settings object
     */
    T create(CreateAgentRequest request, PluginSettings settings) throws Exception;

    /**
     * This message is sent to assist the plugin to refresh any metadata about the agent.
     * The implementation is expected to connect to the cloud provider
     * and make a call to check if the instance is alive and operating well.
     *
     * @param agentId  the elastic agent id
     * @param settings the plugin settings object
     * @throws AgentNotFoundException if there was an error refreshing, or finding the agent
     */
    void refresh(String agentId, PluginSettings settings) throws Exception;

    /**
     * This message is sent when the plugin needs to terminate the agent instance.
     *
     * @param agentId  the elastic agent id
     * @param settings the plugin settings object
     * @throws AgentNotFoundException if the agent instance could not be found
     */
    void terminate(String agentId, PluginSettings settings) throws Exception;

    /**
     * This message is sent from the {@link cd.go.contrib.elasticagents.docker.executors.ServerPingRequestExecutor}
     * to terminate instances that did not register with the server after a timeout. The timeout may be configurable and
     * set via the {@link PluginSettings} instance that is passed in.
     *
     * @param settings the plugin settings object
     * @param agents   the list of all the agents
     */
    void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception;

    /**
     * This message is sent from the {@link cd.go.contrib.elasticagents.docker.executors.ServerPingRequestExecutor}
     * to filter out any new agents, that have registered before the timeout period. The timeout may be configurable and
     * set via the {@link PluginSettings} instance that is passed in.
     *
     * @param settings the plugin settings object
     * @param agents   the list of all the agents (this object must not be mutated)
     * @return a list of agents which were created after {@link PluginSettings#getAutoRegisterPeriod()} ago.
     */
    Agents agentsCreatedBeforeTimeout(PluginSettings settings, Agents agents);
}

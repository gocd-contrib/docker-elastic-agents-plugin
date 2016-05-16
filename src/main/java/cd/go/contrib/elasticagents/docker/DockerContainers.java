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

import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class DockerContainers implements AgentInstances<DockerContainer> {

    private final ConcurrentHashMap<String, DockerContainer> containers = new ConcurrentHashMap<>();

    @Override
    public DockerContainer create(CreateAgentRequest request, PluginSettings settings) throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker(settings));
        register(container);
        return container;
    }

    @Override
    public void refresh(String containerId, PluginSettings settings) throws Exception {
        if (!containers.containsKey(containerId)) {
            register(DockerContainer.find(docker(settings), containerId));
        }
    }

    @Override
    public void terminate(String containerId, PluginSettings settings) throws Exception {
        DockerContainer dockerContainer = containers.get(containerId);
        if (dockerContainer != null) {
            dockerContainer.terminate(docker(settings));
        } else {
            DockerPlugin.LOG.warn("Requested to terminate an instance that does not exist " + containerId);
        }

        containers.remove(containerId);
    }

    @Override
    public void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception {
        DockerContainers toTerminate = unregisteredAfterTimeout(settings, agents);

        if (toTerminate.containers.isEmpty()) {
            return;
        }

        LOG.warn("Terminating instances that did not register " + toTerminate.containers.keySet() + ".");

        for (DockerContainer container : toTerminate.containers.values()) {
            terminate(container.id(), settings);
        }
    }

    private void register(DockerContainer container) {
        containers.put(container.id(), container);
    }

    private DockerContainer dockerContainer(Container container) {
        return new DockerContainer(container.id());
    }

    private DockerClient docker(PluginSettings settings) throws Exception {
        return DockerClientFactory.docker(settings);
    }

    private DockerContainers unregisteredAfterTimeout(PluginSettings settings, Agents knownAgents) throws Exception {
        Period period = settings.getAutoRegisterPeriod();
        DockerContainers unregisteredContainers = new DockerContainers();
        List<Container> allContainers = docker(settings).listContainers(DockerClient.ListContainersParam.withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID));

        for (Container container : allContainers) {
            if (knownAgents.containsAgentWithId(container.id())) {
                continue;
            }

            DateTime dateTimeCreated = new DateTime(container.created() * 1000);

            if (dateTimeCreated.plus(period).isBeforeNow()) {
                unregisteredContainers.register(dockerContainer(container));
            }
        }
        return unregisteredContainers;
    }

    public boolean hasContainer(String agentId) {
        return containers.containsKey(agentId);
    }
}

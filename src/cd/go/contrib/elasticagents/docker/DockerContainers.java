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

public class DockerContainers extends ConcurrentHashMap<String, DockerContainer> {

    private final PluginSettingsRequest pluginSettingsRequest;

    public DockerContainers(PluginSettingsRequest pluginSettingsRequest) {
        this.pluginSettingsRequest = pluginSettingsRequest;
    }

    public DockerContainers(DockerContainers containers) {
        super(containers);
        this.pluginSettingsRequest = containers.pluginSettingsRequest;
    }

    public DockerContainer create(CreateAgentRequest request) throws Exception {
        DockerContainer container = new DockerContainer().create(request, pluginSettingsRequest.getConfiguration(), pluginSettingsRequest.docker());
        this.put(container);
        return container;
    }

    public void refresh(String containerId) throws Exception {
        if (!containsKey(containerId)) {
            this.put(DockerContainer.find(pluginSettingsRequest.docker(), containerId));
        }
    }

    private DockerContainer dockerContainer(Container container) {
        return new DockerContainer(container.id(), new DateTime(container.created()));
    }

    public DockerContainers unregisteredForMoreThan(Period period) throws Exception {
        DockerContainers unregisteredContainers = new DockerContainers(pluginSettingsRequest);
        List<Container> allContainers = pluginSettingsRequest.docker().listContainers(DockerClient.ListContainersParam.withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID));

        for (Container container : allContainers) {
            if (!this.containsKey(container.id())) {
                DateTime dateTimeCreated = new DateTime(container.created());

                if (dateTimeCreated.plus(period).isBeforeNow()) {
                    unregisteredContainers.put(dockerContainer(container));
                }
            }
        }

        return unregisteredContainers;
    }

    private void put(DockerContainer container) {
        this.put(container.id(), container);
    }

    public void terminate(String containerId) throws Exception {
        DockerContainer dockerContainer = this.get(containerId);
        if (dockerContainer != null) {
            dockerContainer.terminate(pluginSettingsRequest.docker());
        } else {
            DockerPlugin.LOG.warn("Requested to terminate an instance that does not exist " + dockerContainer.id());
        }

        this.remove(containerId);
    }

    public void terminateAll() throws Exception {
        for (DockerContainer offlineContainer : values()) {
            offlineContainer.terminate(pluginSettingsRequest.docker());
        }
    }
}

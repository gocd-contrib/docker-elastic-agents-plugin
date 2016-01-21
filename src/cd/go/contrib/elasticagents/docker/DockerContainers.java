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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DockerContainers extends ConcurrentHashMap<String, DockerContainer> {

    private final DefaultDockerClient docker;

    public DockerContainers(DefaultDockerClient docker) {
        this.docker = docker;
    }

    public DockerContainers(DockerContainers containers) {
        super(containers);
        this.docker = containers.docker;
    }

    public DockerContainer create(CreateAgentRequest request) throws InterruptedException, DockerException, IOException {
        DockerContainer container = new DockerContainer(docker).initialize(request.autoRegisterKey(), request.resources(), request.environment());
        this.put(container);
        return container;
    }

    public void refresh(String containerId) throws DockerException, InterruptedException {
        if (!containsKey(containerId)) {
            this.put(DockerContainer.find(docker, containerId));
        }
    }

    private DockerContainer dockerContainer(Container container) {
        return new DockerContainer(docker, container.id(), new DateTime(container.created()));
    }

    public DockerContainers unregisteredForMoreThan(Period period) throws DockerException, InterruptedException {
        DockerContainers unregisteredContainers = new DockerContainers(docker);
        List<Container> allContainers = docker.listContainers(DockerClient.ListContainersParam.withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID));

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

    public void terminate(String containerId) throws DockerException, InterruptedException {
        this.get(containerId).terminate();

        this.remove(containerId);
    }

    public void terminateAll() throws DockerException, InterruptedException {
        for (DockerContainer offlineContainer : values()) {
            offlineContainer.terminate();
        }
    }
}

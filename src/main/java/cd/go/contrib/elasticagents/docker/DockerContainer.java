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
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecState;

import java.io.IOException;
import java.util.HashMap;

import static cd.go.contrib.elasticagents.docker.Constants.CREATED_BY_LABEL_KEY;
import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class DockerContainer {
    private String id;

    public DockerContainer(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static DockerContainer find(DockerClient docker, String containerId) throws DockerException, InterruptedException {
        return new DockerContainer(docker.inspectContainer(containerId).id());
    }

    public void terminate(DockerClient docker) throws DockerException, InterruptedException {
        try {
            LOG.debug("Terminating instance " + this.id() + ".");
            docker.stopContainer(id, 2);
            docker.removeContainer(id);
        } catch (ContainerNotFoundException ignore) {
            LOG.warn("Cannot terminate a container that does not exist " + id);
        }
    }

    public static DockerContainer create(CreateAgentRequest request, PluginSettings settings, DockerClient docker) throws InterruptedException, DockerException, IOException {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        ContainerCreation container = docker.createContainer(ContainerConfig.builder().
                image("gocdcontrib/ubuntu-docker-elastic-agent").
                openStdin(true).
                cmd("bash").
                labels(labels).
                build());
        String id = container.id();

        LOG.debug("Created container " + id);

        docker.startContainer(id);

        DockerContainer dockerContainer = new DockerContainer(id);

        dockerContainer.initialize(request, settings.getGoServerUrl(), docker);
        dockerContainer.startAgent(docker);

        return dockerContainer;
    }

    private void startAgent(DockerClient docker) throws DockerException, InterruptedException {
        LOG.debug("Starting agent process on container " + id);
        AgentInitializerFactory.create(this, docker).startAgent();
        LOG.debug("Agent should now be ready in a moment...");
    }

    private void initialize(CreateAgentRequest request, String goServerUrl, DockerClient docker) throws InterruptedException, DockerException, IOException {
        LOG.debug("Initializing container " + id);
        AgentInitializerFactory.create(this, docker).initialize(goServerUrl, request.autoregisterPropertiesAsString(id));
        LOG.debug("Done initializing container " + id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerContainer that = (DockerContainer) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    void execCommand(String containerId, boolean detach, DockerClient docker, String... cmd) throws DockerException, InterruptedException {
        String execId = docker.execCreate(containerId, cmd);
        if (detach) {
            docker.execStart(execId, DockerClient.ExecStartParameter.DETACH);
            return;
        }

        LogStream logStream = docker.execStart(execId);

        ExecState execState;
        while (true) {
            execState = docker.execInspect(execId);
            if (execState.running()) {
                Thread.sleep(100);
            } else {
                break;
            }
        }

        if (execState.exitCode() != 0) {
            throw new RuntimeException("Could not execute command. The status code was " + execState.exitCode() + ". The output was: " + logStream.readFully());
        }
    }


}

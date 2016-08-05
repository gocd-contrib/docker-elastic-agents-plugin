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
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecState;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static cd.go.contrib.elasticagents.docker.Constants.CREATED_BY_LABEL_KEY;
import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class DockerContainer {
    private final DateTime createdAt;
    private String id;

    public DockerContainer(String id, Date createdAt) {
        this.id = id;
        this.createdAt = new DateTime(createdAt);
    }

    public String id() {
        return id;
    }

    public DateTime createdAt() {
        return createdAt;
    }

    public static DockerContainer find(DockerClient docker, String containerId) throws DockerException, InterruptedException {
        ContainerInfo container = docker.inspectContainer(containerId);
        return new DockerContainer(container.id(), container.created());
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
        String containerName = UUID.randomUUID().toString();
        labels.put(CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        ContainerCreation container = docker.createContainer(ContainerConfig.builder().
                image("gocdcontrib/ubuntu-docker-elastic-agent").
                labels(labels).
                env(
                        "MODE=" + mode(),
                        "GO_SERVER_URL=" + settings.getGoServerUrl(),
                        "AUTO_REGISTER_CONTENTS=" + request.autoregisterPropertiesAsString(containerName)
                ).
                build(), containerName);
        String id = container.id();

        ContainerInfo containerInfo = docker.inspectContainer(id);

        LOG.debug("Created container " + id);

        docker.startContainer(id);

        return new DockerContainer(id, containerInfo.created());
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

    private static String mode() {
        if ("false".equals(System.getProperty("rails.use.compressed.js"))) {
            return "dev";
        }

        if ("true".equalsIgnoreCase(System.getProperty("rails.use.compressed.js"))) {
            return "prod";
        }

        return "";
    }

}

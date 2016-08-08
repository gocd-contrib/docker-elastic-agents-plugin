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
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecState;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cd.go.contrib.elasticagents.docker.Constants.*;
import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class DockerContainer {
    private final DateTime createdAt;
    private final Map<String, String> properties;
    private final String environment;
    private String name;

    public DockerContainer(String name, Date createdAt, Map<String, String> properties, String environment) {
        this.name = name;
        this.createdAt = new DateTime(createdAt);
        this.properties = properties;
        this.environment = environment;
    }

    public String name() {
        return name;
    }

    public DateTime createdAt() {
        return createdAt;
    }

    public String environment() {
        return environment;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public static DockerContainer find(DockerClient docker, String containerId) throws DockerException, InterruptedException {
        return fromContainerInfo(docker.inspectContainer(containerId));
    }

    public static DockerContainer fromContainerInfo(ContainerInfo container) {
        Map<String, String> labels = container.config().labels();
        return new DockerContainer(container.name().substring(1), container.created(), new Gson().fromJson(labels.get(Constants.CONFIGURATION_LABEL_KEY), HashMap.class), labels.get(Constants.ENVIRONMENT_LABEL_KEY));
    }

    public void terminate(DockerClient docker) throws DockerException, InterruptedException {
        try {
            LOG.debug("Terminating instance " + this.name());
            docker.stopContainer(name, 2);
            docker.removeContainer(name);
        } catch (ContainerNotFoundException ignore) {
            LOG.warn("Cannot terminate a container that does not exist " + name);
        }
    }

    public static DockerContainer create(CreateAgentRequest request, PluginSettings settings, DockerClient docker) throws InterruptedException, DockerException, IOException {
        HashMap<String, String> labels = new HashMap<>();
        String containerName = UUID.randomUUID().toString();
        labels.put(CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        if (StringUtils.isNotBlank(request.environment())) {
            labels.put(ENVIRONMENT_LABEL_KEY, request.environment());
        }
        labels.put(CONFIGURATION_LABEL_KEY, new Gson().toJson(request.properties()));

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

        LOG.debug("Created container " + containerName);
        docker.startContainer(containerName);

        return new DockerContainer(containerName, containerInfo.created(), request.properties(), request.environment());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerContainer that = (DockerContainer) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
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

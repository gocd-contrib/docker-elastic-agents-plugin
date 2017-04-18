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
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;

import static cd.go.contrib.elasticagents.docker.Constants.*;
import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.docker.utils.Util.splitIntoLinesAndTrimSpaces;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerContainer {
    private static final Gson GSON = new Gson();
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

    public void terminate(DockerClient docker) throws DockerException, InterruptedException {
        try {
            LOG.debug("Terminating instance " + this.name());
            docker.stopContainer(name, 2);
            docker.removeContainer(name);
        } catch (ContainerNotFoundException ignore) {
            LOG.warn("Cannot terminate a container that does not exist " + name);
        }
    }

    public static DockerContainer fromContainerInfo(ContainerInfo container) {
        Map<String, String> labels = container.config().labels();
        return new DockerContainer(container.name().substring(1), container.created(), GSON.fromJson(labels.get(Constants.CONFIGURATION_LABEL_KEY), HashMap.class), labels.get(Constants.ENVIRONMENT_LABEL_KEY));
    }

    public static DockerContainer create(CreateAgentRequest request, PluginSettings settings, DockerClient docker) throws InterruptedException, DockerException, IOException {
        String containerName = UUID.randomUUID().toString();

        HashMap<String, String> labels = labelsFrom(request);
        String imageName = image(request.properties());
        List<String> env = environmentFrom(request, settings, containerName);

        try {
            docker.inspectImage(imageName);
        } catch (ImageNotFoundException ex) {
            LOG.info("Image " + imageName + " not found, attempting to download.");
            docker.pull(imageName);
        }

        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder();
        if (StringUtils.isNotBlank(request.properties().get("Command"))) {
            containerConfigBuilder.cmd(splitIntoLinesAndTrimSpaces(request.properties().get("Command")).toArray(new String[]{}));
        }

        ContainerConfig containerConfig = containerConfigBuilder.
                image(imageName).
                labels(labels).
                env(env).
                build();
        ContainerCreation container = docker.createContainer(containerConfig, containerName);
        String id = container.id();

        ContainerInfo containerInfo = docker.inspectContainer(id);

        LOG.debug("Created container " + containerName);
        docker.startContainer(containerName);
        LOG.debug("container " + containerName + " started");
        return new DockerContainer(containerName, containerInfo.created(), request.properties(), request.environment());
    }

    private static List<String> environmentFrom(CreateAgentRequest request, PluginSettings settings, String containerName) {
        Set<String> env = new HashSet<>();

        env.addAll(settings.getEnvironmentVariables());
        if (StringUtils.isNotBlank(request.properties().get("Environment"))) {
            env.addAll(splitIntoLinesAndTrimSpaces(request.properties().get("Environment")));
        }

        env.addAll(Arrays.asList(
                "GO_EA_MODE=" + mode(),
                "GO_EA_SERVER_URL=" + settings.getGoServerUrl()
        ));

        env.addAll(request.autoregisterPropertiesAsEnvironmentVars(containerName));

        return new ArrayList<>(env);
    }

    private static HashMap<String, String> labelsFrom(CreateAgentRequest request) {
        HashMap<String, String> labels = new HashMap<>();

        labels.put(CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        if (StringUtils.isNotBlank(request.environment())) {
            labels.put(ENVIRONMENT_LABEL_KEY, request.environment());
        }
        labels.put(CONFIGURATION_LABEL_KEY, GSON.toJson(request.properties()));
        return labels;
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

    private static String mode() {
        if ("false".equals(System.getProperty("rails.use.compressed.js"))) {
            return "dev";
        }

        if ("true".equalsIgnoreCase(System.getProperty("rails.use.compressed.js"))) {
            return "prod";
        }

        return "";
    }

    private static String image(Map<String, String> properties) {
        String image = properties.get("Image");

        if (isBlank(image)) {
            throw new IllegalArgumentException("Must provide `Image` attribute.");
        }

        if (!image.contains(":")) {
            return image + ":latest";
        }
        return image;
    }

}

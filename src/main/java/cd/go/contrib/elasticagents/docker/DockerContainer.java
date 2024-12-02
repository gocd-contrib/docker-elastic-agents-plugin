/*
 * Copyright 2022 Thoughtworks, Inc.
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

import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ContainerStatusReport;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.utils.Util;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

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
    private final String id;
    private String name;
    private final JobIdentifier jobIdentifier;

    public DockerContainer(String id, String name, JobIdentifier jobIdentifier, Date createdAt, Map<String, String> properties, String environment) {
        this.id = id;
        this.name = name;
        this.jobIdentifier = jobIdentifier;
        this.createdAt = new DateTime(createdAt);
        this.properties = properties;
        this.environment = environment;
    }

    public String name() {
        return name;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
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
            docker.removeContainer(name, DockerClient.RemoveContainerParam.removeVolumes());
        } catch (ContainerNotFoundException ignore) {
            LOG.warn("Cannot terminate a container that does not exist " + name);
        }
    }

    public static DockerContainer fromContainerInfo(ContainerInfo container) {
        Map<String, String> labels = container.config().labels();
        return new DockerContainer(container.id(), container.name().substring(1), jobIdentifier(container), container.created(), GSON.fromJson(labels.get(Constants.CONFIGURATION_LABEL_KEY), HashMap.class), labels.get(Constants.ENVIRONMENT_LABEL_KEY));
    }

    public static DockerContainer create(CreateAgentRequest request, PluginSettings settings, DockerClient docker,
                                         ConsoleLogAppender consoleLogAppender) throws InterruptedException, DockerException {
        String containerName = UUID.randomUUID().toString();

        HashMap<String, String> labels = labelsFrom(request);
        String imageName = image(request.properties());
        List<String> env = environmentFrom(request, settings, containerName);

        try {
            docker.inspectImage(imageName);
            if (settings.pullOnContainerCreate()) {
                consoleLogAppender.accept("Pulling a fresh version of " + imageName + ".");
                LOG.info("Pulling a fresh version of " + imageName + ".");
                docker.pull(imageName);
            }
        } catch (ImageNotFoundException ex) {
            consoleLogAppender.accept("Image " + imageName + " not found, attempting to download.");
            LOG.info("Image " + imageName + " not found, attempting to download.");
            docker.pull(imageName);
        }

        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder();
        if (StringUtils.isNotBlank(request.properties().get("Command"))) {
            containerConfigBuilder.cmd(splitIntoLinesAndTrimSpaces(request.properties().get("Command")).toArray(new String[]{}));
        }

        final String hostConfig = request.properties().get("Hosts");
        final String networks = request.properties().get("Networks");
        final String reservedMemory = request.properties().get("ReservedMemory");
        final String maxMemory = request.properties().get("MaxMemory");
        final String cpus = request.properties().get("Cpus");
        final String volumeMounts = request.properties().get("Mounts");

        HostConfig.Builder hostBuilder = HostConfig.builder()
                .privileged(privileged(request.properties()))
                .extraHosts(new Hosts(hostConfig))
                .memoryReservation(new MemorySpecification(reservedMemory).getMemory())
                .memory(new MemorySpecification(maxMemory).getMemory());

        CpusSpecification cpusValue = new CpusSpecification(cpus);
        if (cpusValue.getCpus() != null) {
            hostBuilder
                    .cpuPeriod(cpusValue.getCpuPeriod())
                    .cpuQuota(cpusValue.getCpuQuota());
        }
        if (volumeMounts != null) {
            hostBuilder.appendBinds(Util.splitIntoLinesAndTrimSpaces(volumeMounts));
        }
        if (networks != null) {
            String networkMode = Networks.firstMatching(networks, docker.listNetworks());
            if (networkMode != null) {
                hostBuilder.networkMode(networkMode);
            }
        }

        ContainerConfig containerConfig = containerConfigBuilder
                .image(imageName)
                .labels(labels)
                .env(env)
                .hostConfig(hostBuilder.build())
                .build();

        consoleLogAppender.accept(String.format("Creating container: %s", containerName));
        ContainerCreation container = docker.createContainer(containerConfig, containerName);
        String id = container.id();

        ContainerInfo containerInfo = docker.inspectContainer(id);

        LOG.debug("Created container " + containerName);
        consoleLogAppender.accept(String.format("Starting container: %s", containerName));
        docker.startContainer(containerName);
        Collection<String> additionalNetworks = Networks.getAdditionalNetworks(networks);
        for (String network : additionalNetworks) {
            docker.connectToNetwork(container.id(), network);
            consoleLogAppender.accept(String.format("Connected container %s to additional network: %s", containerName, network));
        }
        consoleLogAppender.accept(String.format("Started container: %s", containerName));
        LOG.debug("container " + containerName + " started");
        return new DockerContainer(id, containerName, request.jobIdentifier(), containerInfo.created(), request.properties(), request.environment());
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
        labels.put(JOB_IDENTIFIER_LABEL_KEY, request.jobIdentifier().toJson());
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

    private static boolean privileged(Map<String, String> properties) {
        String privileged = properties.get("Privileged");
        return "true".equals(privileged);
    }

    public ContainerStatusReport getContainerStatusReport(DockerClient dockerClient) throws Exception {
        ContainerInfo containerInfo = dockerClient.inspectContainer(id);
        return new ContainerStatusReport(id, containerInfo.config().image(), containerInfo.state().status(),
                containerInfo.created().getTime(), jobIdentifier(containerInfo), name);
    }

    public AgentStatusReport getAgentStatusReport(DockerClient dockerClient) throws Exception {
        ContainerInfo containerInfo = dockerClient.inspectContainer(id);
        String logs = readLogs(dockerClient);

        return new AgentStatusReport(jobIdentifier(containerInfo), name, containerInfo.created().getTime(),
                containerInfo.config().image(), containerInfo.path(), containerInfo.networkSettings().ipAddress(), logs,
                parseEnvironmentVariables(containerInfo), extraHosts(containerInfo));
    }

    private static Map<String, String> parseEnvironmentVariables(ContainerInfo containerInfo) {
        ImmutableList<String> env = containerInfo.config().env();
        Map<String, String> environmentVariables = new HashMap<>();
        if (env != null) {
            env.forEach(e -> {
                String[] keyValue = e.split("=");
                if (keyValue.length == 2) {
                    environmentVariables.put(keyValue[0], keyValue[1]);
                } else {
                    environmentVariables.put(keyValue[0], null);
                }
            });
        }
        return environmentVariables;
    }

    private static List<String> extraHosts(ContainerInfo containerInfo) {
        HostConfig hostConfig = containerInfo.hostConfig();
        if (hostConfig != null) {
           return hostConfig.extraHosts();
        }
        return new ArrayList<>();
    }

    private static JobIdentifier jobIdentifier(ContainerInfo containerInfo) {
        ImmutableMap<String, String> labels = containerInfo.config().labels();
        if (labels == null) {
            return null;
        }
        return JobIdentifier.fromJson(labels.get(JOB_IDENTIFIER_LABEL_KEY));
    }

    private String readLogs(DockerClient dockerClient) {
        try {
            return dockerClient.logs(id, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr()).readFully();
        } catch (Exception e) {
            LOG.debug("Could not fetch logs", e);
            return "";
        }
    }
}

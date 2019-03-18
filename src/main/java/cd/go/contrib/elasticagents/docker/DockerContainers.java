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

import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ContainerStatusReport;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Info;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.docker.utils.Util.readableSize;

public class DockerContainers implements AgentInstances<DockerContainer> {

    private final Map<String, DockerContainer> instances = new ConcurrentHashMap<>();
    private List<JobIdentifier> jobsWaitingForAgentCreation = new ArrayList<>();
    private boolean refreshed;
    public Clock clock = Clock.DEFAULT;

    final Semaphore semaphore = new Semaphore(0, true);

    @Override
    public DockerContainer create(CreateAgentRequest request, PluginRequest pluginRequest) throws Exception {
        LOG.info(String.format("[Create Agent] Processing create agent request for %s", request.jobIdentifier()));
        ClusterProfile settings = request.getClusterProfileProperties();
        final Integer maxAllowedContainers = settings.getMaxDockerContainers();
        synchronized (instances) {
            if (!jobsWaitingForAgentCreation.contains(request.jobIdentifier())) {
                jobsWaitingForAgentCreation.add(request.jobIdentifier());
            }
            doWithLockOnSemaphore(new SetupSemaphore(maxAllowedContainers, instances, semaphore));
            List<Map<String, String>> messages = new ArrayList<>();
            if (semaphore.tryAcquire()) {
                pluginRequest.addServerHealthMessage(messages);
                DockerContainer container = DockerContainer.create(request, settings, docker(settings));
                register(container);
                jobsWaitingForAgentCreation.remove(request.jobIdentifier());
                return container;
            } else {
                String maxLimitExceededMessage = String.format("The number of containers currently running is currently at the maximum permissible limit, \"%d\". Not creating more containers for jobs: %s.", instances.size(), jobsWaitingForAgentCreation.stream().map(JobIdentifier::getRepresentation)
                        .collect(Collectors.joining(", ")));
                Map<String, String> messageToBeAdded = new HashMap<>();
                messageToBeAdded.put("type", "warning");
                messageToBeAdded.put("message", maxLimitExceededMessage);
                messages.add(messageToBeAdded);
                pluginRequest.addServerHealthMessage(messages);
                LOG.info(maxLimitExceededMessage);
                return null;
            }
        }
    }

    private void doWithLockOnSemaphore(Runnable runnable) {
        synchronized (semaphore) {
            runnable.run();
        }
    }

    @Override
    public void terminate(String agentId, PluginSettings settings) throws Exception {
        DockerContainer instance = instances.get(agentId);
        if (instance != null) {
            instance.terminate(docker(settings));
        } else {
            LOG.warn("Requested to terminate an instance that does not exist " + agentId);
        }

        doWithLockOnSemaphore(new Runnable() {
            @Override
            public void run() {
                semaphore.release();
            }
        });

        synchronized (instances) {
            instances.remove(agentId);
        }
    }

    @Override
    public void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception {
        DockerContainers toTerminate = unregisteredAfterTimeout(settings, agents);
        if (toTerminate.instances.isEmpty()) {
            return;
        }

        LOG.warn("Terminating instances that did not register " + toTerminate.instances.keySet());
        for (DockerContainer container : toTerminate.instances.values()) {
            terminate(container.name(), settings);
        }
    }

    @Override
    public Agents instancesCreatedAfterTimeout(PluginSettings settings, Agents agents) {
        ArrayList<Agent> oldAgents = new ArrayList<>();
        for (Agent agent : agents.agents()) {
            DockerContainer instance = instances.get(agent.elasticAgentId());
            if (instance == null) {
                continue;
            }

            if (clock.now().isAfter(instance.createdAt().plus(settings.getAutoRegisterPeriod()))) {
                oldAgents.add(agent);
            }
        }
        return new Agents(oldAgents);
    }

    @Override
    public void refreshAll(PluginRequest pluginRequest) throws Exception {
        if (!refreshed) {
            DockerClient docker = docker(pluginRequest.getPluginSettings());
            List<Container> containers = docker.listContainers(DockerClient.ListContainersParam.withLabel(Constants.CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID));
            for (Container container : containers) {
                register(DockerContainer.fromContainerInfo(docker.inspectContainer(container.id())));
            }
            refreshed = true;
        }
    }

    public StatusReport getStatusReport(PluginSettings pluginSettings) throws Exception {
        DockerClient dockerClient = DockerClientFactory.docker(pluginSettings);

        Info info = dockerClient.info();
        return new StatusReport(info.osType(), info.architecture(), info.serverVersion(),
            info.cpus(), readableSize(info.memTotal()), getContainerStatus(dockerClient));
    }

    public AgentStatusReport getAgentStatusReport(PluginSettings pluginSettings, DockerContainer dockerContainer) throws Exception {
        return dockerContainer.getAgentStatusReport(docker(pluginSettings));
    }

    private List<ContainerStatusReport> getContainerStatus(DockerClient dockerClient) throws Exception {
        List<ContainerStatusReport> containerStatusReportList = new ArrayList<>();
        for (DockerContainer dockerContainer : instances.values()) {
            containerStatusReportList.add(dockerContainer.getContainerStatusReport(dockerClient));
        }
        return containerStatusReportList;
    }

    private void register(DockerContainer container) {
        instances.put(container.name(), container);
    }

    private DockerClient docker(PluginSettings settings) throws Exception {
        return DockerClientFactory.docker(settings);
    }

    private DockerContainers unregisteredAfterTimeout(PluginSettings settings, Agents knownAgents) throws Exception {
        Period period = settings.getAutoRegisterPeriod();
        DockerContainers unregisteredContainers = new DockerContainers();

        for (String containerName : instances.keySet()) {
            if (knownAgents.containsAgentWithId(containerName)) {
                continue;
            }

            ContainerInfo containerInfo;
            try {
                containerInfo = docker(settings).inspectContainer(containerName);
            } catch (ContainerNotFoundException e) {
                LOG.warn("The container " + containerName + " could not be found.");
                continue;
            }
            DateTime dateTimeCreated = new DateTime(containerInfo.created());

            if (clock.now().isAfter(dateTimeCreated.plus(period))) {
                unregisteredContainers.register(DockerContainer.fromContainerInfo(containerInfo));
            }
        }
        return unregisteredContainers;
    }

    public boolean hasInstance(String agentId) {
        return instances.containsKey(agentId);
    }

    @Override
    public DockerContainer find(String agentId) {
        return instances.get(agentId);
    }

    public Optional<DockerContainer> find(JobIdentifier jobIdentifier) {
        return instances.values()
            .stream()
            .filter(instance -> instance.getJobIdentifier().equals(jobIdentifier))
            .findFirst();
    }

    // used by test
    protected boolean isEmpty() {
        return instances.isEmpty();
    }
}

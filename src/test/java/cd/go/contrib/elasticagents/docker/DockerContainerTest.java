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
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class DockerContainerTest extends BaseTest {

    private CreateAgentRequest request;

    private JobIdentifier jobIdentifier;
    private ConsoleLogAppender consoleLogAppender;

    @BeforeEach
    public void setUp() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        jobIdentifier = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 1L);
        request = new CreateAgentRequest("key", properties, "production", jobIdentifier, Collections.emptyMap());
        consoleLogAppender = mock(ConsoleLogAppender.class);
    }

    @Test
    public void shouldCreateContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());
        assertContainerExist(container.name());
    }

    @Test
    public void shouldPullAnImageWhenOneDoesNotExist() throws Exception {
        String imageName = "busybox:latest";

        try {
            docker.removeImage(imageName, true, false);
        } catch (ImageNotFoundException ignore) {
        }
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        assertNotNull(docker.inspectImage(imageName));
        assertContainerExist(container.name());
    }

    @Test
    public void shouldRaiseExceptionWhenImageIsNotFoundInDockerRegistry() throws Exception {
        String imageName = "ubuntu:does-not-exist";
        assertThatThrownBy(() -> DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessageContaining("Image not found: " + imageName);
    }

    @Test
    public void shouldNotCreateContainerIfTheImageIsNotProvided() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest("key", new HashMap<>(), "production", jobIdentifier, Collections.emptyMap());

        assertThatThrownBy(() -> DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Must provide `Image` attribute.");;
    }

    @Test
    public void shouldStartContainerWithCorrectEnvironment() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Environment", "A=B\nC=D\r\nE=F\n\n\nX=Y");

        PluginSettings clusterProfiles = createClusterProfiles();
        clusterProfiles.setEnvironmentVariables("GLOBAL=something");
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.config().env(), hasItems("A=B", "C=D", "E=F", "X=Y", "GLOBAL=something"));
        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);

        assertThat(dockerContainer.properties(), is(properties));
    }

    @Test
    public void shouldStartContainerWithPrivilegedMode() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Privileged", "true");

        PluginSettings clusterProfiles = createClusterProfiles();
        clusterProfiles.setEnvironmentVariables("GLOBAL=something");
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().privileged(), is(true));
        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);

        assertThat(dockerContainer.properties(), is(properties));
    }

    @Test
    public void shouldStartContainerWithAutoregisterEnvironmentVariables() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");

        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());
        assertThat(containerInfo.config().env(), hasItem("GO_EA_AUTO_REGISTER_KEY=key"));
        assertThat(containerInfo.config().env(), hasItem("GO_EA_AUTO_REGISTER_ENVIRONMENT=prod"));
        assertThat(containerInfo.config().env(), hasItem("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + container.name()));
        assertThat(containerInfo.config().env(), hasItem("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + Constants.PLUGIN_ID));
    }

    @Test
    public void shouldStartContainerWithCorrectCommand() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Command", "cat\n/etc/hosts\n/etc/group");

        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());
        assertThat(containerInfo.config().cmd(), is(Arrays.asList("cat", "/etc/hosts", "/etc/group")));
        String logs = docker.logs(container.name(), DockerClient.LogsParam.stdout()).readFully();
        assertThat(logs, containsString("127.0.0.1")); // from /etc/hosts
        assertThat(logs, containsString("floppy:x:19:")); // from /etc/group
    }

    @Test
    public void shouldTerminateAnExistingContainerAndRemoveAssociatedVolumes() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "docker:dind"); // Container with implicit anonymous VOLUME
        properties.put("Command", "/bin/sleep\n5");
        request = new CreateAgentRequest("key", properties, "production", jobIdentifier, Collections.emptyMap());

        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker,
                consoleLogAppender);
        String containerName = container.name();
        String volumeName = docker.inspectContainer(containerName).mounts().get(0).name();

        containers.add(containerName);
        container.terminate(docker);

        assertContainerDoesNotExist(containerName);
        assertVolumeDoesNotExist(volumeName);
    }

    @Test
    public void shouldFindAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(docker.inspectContainer(container.name()));

        assertEquals(container, dockerContainer);
    }

    @Test
    public void shouldStartContainerWithHostEntry() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine:latest");
        properties.put("Hosts", "127.0.0.2\tbaz \n192.168.5.1\tfoo\tbar\n127.0.0.1  gocd.local ");
        properties.put("Command", "cat\n/etc/hosts");

        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender);

        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        final ImmutableList<String> extraHosts = containerInfo.hostConfig().extraHosts();
        assertThat(extraHosts, containsInAnyOrder(
                "baz:127.0.0.2", "foo\tbar:192.168.5.1", "gocd.local:127.0.0.1"
        ));

        String logs = docker.logs(container.name(), DockerClient.LogsParam.stdout()).readFully();
        assertThat(logs, containsString("127.0.0.2\tbaz"));
        assertThat(logs, containsString("192.168.5.1\tfoo"));
        assertThat(logs, containsString("127.0.0.1\tgocd.local"));

        AgentStatusReport agentStatusReport = container.getAgentStatusReport(docker);
        assertThat(agentStatusReport.getHosts(), containsInAnyOrder(
                "baz:127.0.0.2", "foo\tbar:192.168.5.1", "gocd.local:127.0.0.1"));
    }

    @Test
    public void shouldGetContainerStatusReport() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        ContainerStatusReport containerStatusReport = container.getContainerStatusReport(docker);

        assertThat(containerStatusReport, is(notNullValue()));
        assertThat(containerStatusReport.getElasticAgentId(), is(container.name()));
        assertThat(containerStatusReport.getImage(), is("busybox:latest"));
        assertThat(containerStatusReport.getJobIdentifier(), is(jobIdentifier));
        assertThat(containerStatusReport.getState(), equalToIgnoringCase("exited"));
    }

    @Test
    public void shouldGetAgentStatusReport() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Command", "ls");
        properties.put("Environment", "A=B\nC=D");
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        AgentStatusReport agentStatusReport = container.getAgentStatusReport(docker);

        assertThat(agentStatusReport, is(notNullValue()));
        assertThat(agentStatusReport.getElasticAgentId(), is(container.name()));
        assertThat(agentStatusReport.getImage(), is("busybox:latest"));
        assertThat(agentStatusReport.getJobIdentifier(), is(jobIdentifier));
        assertThat(agentStatusReport.getCommand(), is("ls"));
        Map<String, String> environmentVariables = agentStatusReport.getEnvironmentVariables();
        assertThat(environmentVariables, hasEntry("A", "B"));
        assertThat(environmentVariables, hasEntry("C", "D"));
    }

    @Test
    public void shouldPullImageWhenPullSettingIsEnabled() throws Exception {
        String imageName = "busybox:latest";
        PluginSettings clusterProfiles = createClusterProfiles();
        clusterProfiles.setPullOnContainerCreate(true);

        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        assertContainerExist(container.name());
        containers.add(container.name());

        assertNotNull(docker.inspectImage(imageName));
        assertContainerExist(container.name());
    }

    @Test
    public void shouldStartContainerWithMemoryLimits() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("ReservedMemory", "6M");
        properties.put("MaxMemory", "10M");

        PluginSettings clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().memoryReservation(), is(6 * 1024 * 1024L));
        assertThat(containerInfo.hostConfig().memory(), is(10 * 1024 * 1024L));

        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);

        assertThat(dockerContainer.properties(), is(properties));
    }

    @Test
    public void shouldStartContainerNoMemoryLimits() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("ReservedMemory", "");
        properties.put("MaxMemory", "");

        PluginSettings clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().memoryReservation(), is(0L));
        assertThat(containerInfo.hostConfig().memory(), is(0L));
    }

    @Test
    public void shouldStartContainerWithCpuLimit() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Cpus", ".75");

        PluginSettings clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().cpuPeriod(), is(100_000L));
        assertThat(containerInfo.hostConfig().cpuQuota(), is(75_000L));

        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);

        assertThat(dockerContainer.properties(), is(properties));
    }

    @Test
    public void shouldStartContainerWithMountedVolumes() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        // using "/" as source folder because it seems the folder must exist on testing machine
        properties.put("Mounts", "/:/A\n/:/B:ro");

        PluginSettings clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod", jobIdentifier, Collections.emptyMap()), clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().binds(), containsInAnyOrder(
                "/:/A", "/:/B:ro"));

        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);

        assertThat(dockerContainer.properties(), is(properties));
    }
}

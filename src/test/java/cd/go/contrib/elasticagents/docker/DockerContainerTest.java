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
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DockerContainerTest extends BaseTest {

    private CreateAgentRequest request;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "alpine");
        properties.put("Command", "/bin/sleep\n5");
        request = new CreateAgentRequest("key", properties, "production");
    }

    @Test
    public void shouldCreateContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
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
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod"), createSettings(), docker);
        containers.add(container.name());

        assertNotNull(docker.inspectImage(imageName));
        assertContainerExist(container.name());
    }

    @Test
    public void shouldRaiseExceptionWhenImageIsNotFoundInDockerRegistry() throws Exception {
        String imageName = "ubuntu:does-not-exist";
        thrown.expect(ImageNotFoundException.class);
        thrown.expectMessage(containsString("Image not found: " + imageName));
        DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod"), createSettings(), docker);
    }

    @Test
    public void shouldNotCreateContainerIfTheImageIsNotProvided() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest("key", new HashMap<String, String>(), "production");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must provide `Image` attribute.");

        DockerContainer.create(request, createSettings(), docker);
    }

    @Test
    public void shouldStartContainerWithCorrectEnvironment() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");
        properties.put("Environment", "A=B\nC=D\r\nE=F\n\n\nX=Y");

        PluginSettings settings = createSettings();
        settings.setEnvironmentVariables("GLOBAL=something");
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod"), settings, docker);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.config().env(), hasItems("A=B", "C=D", "E=F", "X=Y", "GLOBAL=something"));
        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);

        assertThat(dockerContainer.properties(), is(properties));
    }

    @Test
    public void shouldStartContainerWithAutoregisterEnvironmentVariables() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("Image", "busybox:latest");

        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod"), createSettings(), docker);
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

        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", properties, "prod"), createSettings(), docker);
        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());
        assertThat(containerInfo.config().cmd(), is(Arrays.asList("cat", "/etc/hosts", "/etc/group")));
        String logs = docker.logs(container.name(), DockerClient.LogsParam.stdout()).readFully();
        assertThat(logs, containsString("127.0.0.1")); // from /etc/hosts
        assertThat(logs, containsString("floppy:x:19:")); // from /etc/group
    }

    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        containers.add(container.name());

        container.terminate(docker);

        assertContainerDoesNotExist(container.name());
    }

    @Test
    public void shouldFindAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        containers.add(container.name());

        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(docker.inspectContainer(container.name()));

        assertEquals(container, dockerContainer);
    }
}

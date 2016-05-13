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
import com.spotify.docker.client.ContainerNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DockerContainersTest extends BaseTest {

    private CreateAgentRequest request;
    private DockerContainers dockerContainers;
    private PluginSettings settings;

    @Before
    public void setUp() throws Exception {
        request = new CreateAgentRequest("key", new HashMap<String, String>(), "production");
        dockerContainers = new DockerContainers();
        settings = createSettings();
    }

    @Test
    public void shouldCreateADockerInstance() throws Exception {
        DockerContainer container = dockerContainers.create(request, settings);
        containers.add(container.id());

        docker.inspectContainer(container.id());
    }

    @Test(expected = ContainerNotFoundException.class)
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerContainer container = dockerContainers.create(request, settings);
        containers.add(container.id());

        dockerContainers.terminate(container.id(), settings);

        docker.inspectContainer(container.id());
    }


    @Test
    public void shouldRefreshADockerContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, settings, docker);
        containers.add(container.id());

        assertFalse(dockerContainers.containsKey(container.id()));
        dockerContainers.refresh(container.id(), settings);
        assertTrue(dockerContainers.containsKey(container.id()));
    }

//    @Test
//    public void shouldTerminateAllContainers() throws Exception {
//        DockerContainer container1 = dockerContainers.create(request, settings);
//        DockerContainer container2 = dockerContainers.create(request, settings);
//
//        containers.add(container1.id());
//        containers.add(container2.id());
//
//        dockerContainers.terminateAll(settings);
//
//        assertTrue(dockerContainers.isEmpty());
//
//        assertContainerDoesNotExist(container1.id());
//        assertContainerDoesNotExist(container2.id());
//    }

}

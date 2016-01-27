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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DockerContainerTest extends BaseTest {

    private CreateAgentRequest request;

    @Before
    public void setUp() throws Exception {
        request = new CreateAgentRequest("key", Arrays.asList("centos", "java"), "production");
    }

    @Test
    public void shouldCreateContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        this.containers.add(container.id());
        docker.inspectContainer(container.id());
    }


    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        this.containers.add(container.id());

        container.terminate(docker);

        assertContainerDoesNotExist(container.id());
    }

    @Test
    public void shouldFindAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        this.containers.add(container.id());

        DockerContainer dockerContainer = DockerContainer.find(docker, container.id());

        assertEquals(container, dockerContainer);
    }
}

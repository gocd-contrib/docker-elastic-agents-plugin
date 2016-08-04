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

import com.google.common.base.Charsets;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ProductionAgentInitializer implements AgentInitializer {

    private final DockerContainer dockerContainer;
    private final DockerClient docker;

    public ProductionAgentInitializer(DockerContainer dockerContainer, DockerClient docker) {
        this.dockerContainer = dockerContainer;
        this.docker = docker;
    }

    @Override
    public void initialize(String goServerUrl, String autoregisterProperties) throws IOException, DockerException, InterruptedException {
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        File configDir = new File(tempDirectory, "config");

        tempDirectory.mkdirs();
        configDir.mkdirs();

        try {
            File autoregisterPropertiesFile = new File(configDir, "autoregister.properties");
            File startupScript = new File(tempDirectory, "docker-agent-start.sh");
            FileUtils.write(startupScript, "#!/bin/bash\n" +
                    "cd /var/lib/go-agent\n" +
                    "DAEMON=Y GO_SERVER_URL='" + goServerUrl + "' ./agent.sh", Charsets.UTF_8);

            FileUtils.write(autoregisterPropertiesFile, autoregisterProperties, Charsets.UTF_8);
            docker.copyToContainer(tempDirectory.toPath(), dockerContainer.id(), "/var/lib/go-agent");
        } finally {
            FileUtils.deleteDirectory(tempDirectory);
        }
    }

    @Override
    public void startAgent() throws DockerException, InterruptedException {
        dockerContainer.execCommand(dockerContainer.id(), false, docker, "bash", "/go-agent/docker-agent-start.sh");
    }
}

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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient.ListVolumesParam;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.VolumeNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import static java.lang.System.getenv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BaseTest {

    protected static DefaultDockerClient.Builder builder;
    protected static DefaultDockerClient docker;
    protected static HashSet<String> containers;

    @BeforeAll
    public static void beforeClass() throws Exception {
        builder = DefaultDockerClient.fromEnv();
        docker = builder.build();
        containers = new HashSet<>();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        for (String container : containers) {
            try {
                docker.inspectContainer(container);
                docker.stopContainer(container, 2);
                docker.removeContainer(container);
            } catch (ContainerNotFoundException ignore) {

            }
        }
    }

    protected ClusterProfileProperties createClusterProfiles() throws IOException {
        ClusterProfileProperties settings = new ClusterProfileProperties();

        settings.setMaxDockerContainers(1);
        settings.setDockerURI(builder.uri().toString());
        if (settings.getDockerURI().startsWith("https://")) {
            settings.setDockerCACert(Files.readString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CA_CERT_NAME), StandardCharsets.UTF_8));
            settings.setDockerClientCert(Files.readString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_CERT_NAME), StandardCharsets.UTF_8));
            settings.setDockerClientKey(Files.readString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_KEY_NAME), StandardCharsets.UTF_8));
        }

        return settings;
    }

    protected void assertContainerDoesNotExist(String id) {
        assertThatThrownBy(() -> docker.inspectContainer(id))
                .isInstanceOf(ContainerNotFoundException.class);
    }

    protected void assertContainerExist(String id) throws DockerException, InterruptedException {
        assertNotNull(docker.inspectContainer(id));
    }

    protected void assertVolumeDoesNotExist(String volumeName) throws DockerException, InterruptedException {
        assertThatThrownBy(() -> docker.inspectVolume(volumeName))
                .isInstanceOf(VolumeNotFoundException.class);
    }
}

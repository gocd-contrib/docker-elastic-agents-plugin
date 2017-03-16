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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.AuthConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerClientFactory {

    private static DefaultDockerClient client;
    private static PluginSettings pluginSettings;

    public static synchronized DockerClient docker(PluginSettings pluginSettings) throws Exception {
        if (pluginSettings.equals(DockerClientFactory.pluginSettings) && DockerClientFactory.client != null) {
            return DockerClientFactory.client;
        }

        DockerClientFactory.pluginSettings = pluginSettings;
        DockerClientFactory.client = createClient(pluginSettings);
        return DockerClientFactory.client;
    }

    private static DefaultDockerClient createClient(PluginSettings pluginSettings) throws Exception {
        DefaultDockerClient.Builder builder = DefaultDockerClient.builder();

        builder.uri(pluginSettings.getDockerURI());
        if (pluginSettings.getDockerURI().startsWith("https://")) {
            setupCerts(pluginSettings, builder);
        }

        if (pluginSettings.getUseDockerAuthInfo()) {
            builder.authConfig(AuthConfig.fromDockerConfig().build());
        }

        DefaultDockerClient docker = builder.build();
        String ping = docker.ping();
        if (!"OK".equals(ping)) {
            throw new RuntimeException("Could not ping the docker server, the server said '" + ping + "' instead of 'OK'.");
        }
        return docker;
    }

    private static void setupCerts(PluginSettings pluginSettings, DefaultDockerClient.Builder builder) throws IOException, DockerCertificateException {
        if (isBlank(pluginSettings.getDockerCACert()) || isBlank(pluginSettings.getDockerClientCert()) || isBlank(pluginSettings.getDockerClientKey())) {
            LOG.warn("Missing docker certificates, will attempt to connect without certificates");
            return;
        }

        Path certificateDir = Files.createTempDirectory(UUID.randomUUID().toString());
        File tempDirectory = certificateDir.toFile();

        try {
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CA_CERT_NAME), pluginSettings.getDockerCACert(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CLIENT_CERT_NAME), pluginSettings.getDockerClientCert(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CLIENT_KEY_NAME), pluginSettings.getDockerClientKey(), StandardCharsets.UTF_8);
            builder.dockerCertificates(new DockerCertificates(certificateDir));
        } finally {
            FileUtils.deleteDirectory(tempDirectory);
        }
    }
}

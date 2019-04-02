/*
 * Copyright 2019 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.*;
import cd.go.contrib.elasticagents.docker.requests.MigrateConfigurationRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class MigrateConfigurationRequestExecutor implements RequestExecutor {

    private MigrateConfigurationRequest migrateConfigurationRequest;

    public MigrateConfigurationRequestExecutor(MigrateConfigurationRequest migrateConfigurationRequest) {
        this.migrateConfigurationRequest = migrateConfigurationRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        PluginSettings pluginSettings = migrateConfigurationRequest.getPluginSettings();
        List<ClusterProfile> existingClusterProfiles = migrateConfigurationRequest.getClusterProfiles();
        List<ElasticAgentProfile> existingElasticAgentProfiles = migrateConfigurationRequest.getElasticAgentProfiles();

        if (!arePluginSettingsConfigured(pluginSettings)) {
            LOG.info("[Migrate Config] No Plugin Settings are configured. Skipping Config Migration.");
            return new DefaultGoPluginApiResponse(200, migrateConfigurationRequest.toJSON());
        }

        if (!existingClusterProfiles.isEmpty()) {
            List<String> existingClusterProfileIds = existingClusterProfiles.stream().map(ClusterProfile::getId).collect(Collectors.toList());
            LOG.info("[Migrate Config] Found already defined cluster profiles {}. Skipping Config Migration.", existingClusterProfileIds);
            return new DefaultGoPluginApiResponse(200, migrateConfigurationRequest.toJSON());
        }

        LOG.info("[Migrate Config] No defined cluster profiles found. Running migrations..");
        String defaultClusterId = UUID.randomUUID().toString();
        ClusterProfile clusterProfile = new ClusterProfile(defaultClusterId, Constants.PLUGIN_ID, pluginSettings);
        existingElasticAgentProfiles.forEach(elasticAgentProfile -> {
            elasticAgentProfile.setClusterProfileId(defaultClusterId);
        });

        MigrateConfigurationRequest migrateConfigurationRequest = new MigrateConfigurationRequest();
        migrateConfigurationRequest.setPluginSettings(pluginSettings);
        migrateConfigurationRequest.setClusterProfiles(Arrays.asList(clusterProfile));
        migrateConfigurationRequest.setElasticAgentProfiles(existingElasticAgentProfiles);

        return new DefaultGoPluginApiResponse(200, migrateConfigurationRequest.toJSON());
    }

    private boolean arePluginSettingsConfigured(PluginSettings pluginSettings) {
        return !StringUtils.isBlank(pluginSettings.getGoServerUrl());
    }
}

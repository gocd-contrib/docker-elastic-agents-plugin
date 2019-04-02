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

import cd.go.contrib.elasticagents.docker.ClusterProfile;
import cd.go.contrib.elasticagents.docker.Constants;
import cd.go.contrib.elasticagents.docker.ElasticAgentProfile;
import cd.go.contrib.elasticagents.docker.PluginSettings;
import cd.go.contrib.elasticagents.docker.requests.MigrateConfigurationRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MigrateConfigurationRequestExecutorTest {

    private PluginSettings pluginSettings;
    private ClusterProfile clusterProfile;
    private ElasticAgentProfile elasticAgentProfile;

    @Before
    public void setUp() throws Exception {
        pluginSettings = new PluginSettings();
        pluginSettings.setGoServerUrl("https://127.0.0.1:8154/go");
        pluginSettings.setAutoRegisterTimeout("20");

        clusterProfile = new ClusterProfile();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId(Constants.PLUGIN_ID);
        clusterProfile.setClusterProfileProperties(pluginSettings);

        elasticAgentProfile = new ElasticAgentProfile();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        HashMap<String, String> properties = new HashMap<>();
        properties.put("some_key", "some_value");
        properties.put("some_key2", "some_value2");
        elasticAgentProfile.setProperties(properties);
    }

    @Test
    public void shouldNotMigrateConfigWhenNoPluginSettingsAreConfigured() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(new PluginSettings(), Arrays.asList(clusterProfile), Arrays.asList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings(), is(new PluginSettings()));
        assertThat(responseObject.getClusterProfiles(), is(Arrays.asList(clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(Arrays.asList(elasticAgentProfile)));
    }

    @Test
    public void shouldNotMigrateConfigWhenClusterProfileIsAlreadyConfigured() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Arrays.asList(clusterProfile), Arrays.asList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings(), is(pluginSettings));
        assertThat(responseObject.getClusterProfiles(), is(Arrays.asList(clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(Arrays.asList(elasticAgentProfile)));
    }

    @Test
    public void shouldDefineANewClusterProfileFromPluginSettings() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Collections.emptyList(), Arrays.asList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        assertThat(responseObject.getPluginSettings(), is(pluginSettings));
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual, is(Arrays.asList(this.clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(Arrays.asList(elasticAgentProfile)));
    }

    @Test
    public void shouldAssociateExistingElasticAgentProfilesWithNewlyDefinedClusterProfile() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(pluginSettings, Collections.emptyList(), Arrays.asList(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor(request);

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = MigrateConfigurationRequest.fromJSON(response.responseBody());

        String newlyDefinedClusterId = responseObject.getClusterProfiles().get(0).getId();
        elasticAgentProfile.setClusterProfileId(newlyDefinedClusterId);

        assertThat(responseObject.getElasticAgentProfiles(), is(Arrays.asList(elasticAgentProfile)));
    }
}

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

import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.requests.JobCompletionRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClusterProfilePropertiesTest {

    @Test
    public void shouldGenerateSameUUIDForClusterProfileProperties() {
        Map<String, String> clusterProfileConfigurations = Collections.singletonMap("go_server_url", "http://go-server-url/go");
        ClusterProfileProperties clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);

        assertThat(clusterProfileProperties.uuid(), is(clusterProfileProperties.uuid()));
    }

    @Test
    public void shouldGenerateSameUUIDForClusterProfilePropertiesAcrossRequests() {
        String createAgentRequestJSON = """
                {
                  "auto_register_key": "secret-key",
                  "elastic_agent_profile_properties": {
                    "key1": "value1",
                    "key2": "value2"
                  },
                  "cluster_profile_properties": {
                    "go_server_url": "https://foo.com/go",
                    "docker_uri": "unix:///var/run/docker.sock"
                  },
                  "environment": "prod"
                }""";

        CreateAgentRequest createAgentRequest = CreateAgentRequest.fromJSON(createAgentRequestJSON);

        String jobCompletionRequestJSON = """
                {
                  "elastic_agent_id": "ea1",
                  "elastic_agent_profile_properties": {
                    "Image": "alpine:latest"
                  },
                  "cluster_profile_properties": {
                    "go_server_url": "https://foo.com/go",\s
                    "docker_uri": "unix:///var/run/docker.sock"
                  },
                  "job_identifier": {
                    "pipeline_name": "test-pipeline",
                    "pipeline_counter": 1,
                    "pipeline_label": "Test Pipeline",
                    "stage_name": "test-stage",
                    "stage_counter": "1",
                    "job_name": "test-job",
                    "job_id": 100
                  }
                }""";

        JobCompletionRequest jobCompletionRequest = JobCompletionRequest.fromJSON(jobCompletionRequestJSON);
        assertThat(jobCompletionRequest.getClusterProfileProperties().uuid(), is(createAgentRequest.getClusterProfileProperties().uuid()));
    }
}

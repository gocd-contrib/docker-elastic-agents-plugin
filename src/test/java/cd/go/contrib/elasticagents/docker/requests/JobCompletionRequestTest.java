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

package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.docker.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JobCompletionRequestTest {
    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        String json = """
                {
                  "elastic_agent_id": "ea1",
                  "elastic_agent_profile_properties": {
                    "Image": "alpine:latest"
                  },
                  "cluster_profile_properties": {
                    "go_server_url": "https://example.com/go"
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

        JobCompletionRequest request = JobCompletionRequest.fromJSON(json);
        JobIdentifier expectedJobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        JobIdentifier actualJobIdentifier = request.jobIdentifier();
        assertThat(actualJobIdentifier, is(expectedJobIdentifier));
        assertThat(request.getElasticAgentId(), is("ea1"));

        HashMap<String, String> propertiesJson = new HashMap<>();
        propertiesJson.put("Image", "alpine:latest");
        assertThat(request.getProperties(), Matchers.equalTo(propertiesJson));

        ClusterProfileProperties expectedClusterProfileProperties = new ClusterProfileProperties();
        expectedClusterProfileProperties.setGoServerUrl("https://example.com/go");
        assertThat(request.getClusterProfileProperties(), is(expectedClusterProfileProperties));
    }
}

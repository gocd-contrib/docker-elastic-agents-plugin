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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.requests.ClusterProfileValidateRequest;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Collections;

public class ClusterProfilePropertiesValidateRequestExecutorTest {
    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(Collections.singletonMap("foo", "bar")));
        String json = executor.execute().responseBody();
        String expectedStr = "[" +
                "  {\"message\":\"Go Server URL must not be blank.\",\"key\":\"go_server_url\"}," +
                "  {\"message\":\"max_docker_containers must not be blank.\",\"key\":\"max_docker_containers\"}," +
                "  {\"message\":\"docker_uri must not be blank.\",\"key\":\"docker_uri\"}," +
                "  {\"message\":\"auto_register_timeout must not be blank.\",\"key\":\"auto_register_timeout\"}," +
                "  {\"key\":\"foo\",\"message\":\"Is an unknown property\"}" +
                "]";
        JSONAssert.assertEquals(expectedStr, json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeys() throws Exception {
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(Collections.<String, String>emptyMap()));
        String json = executor.execute().responseBody();

        String expectedStr = "[" +
                "  {\"message\":\"Go Server URL must not be blank.\",\"key\":\"go_server_url\"}," +
                "  {\"message\":\"max_docker_containers must not be blank.\",\"key\":\"max_docker_containers\"}," +
                "  {\"message\":\"docker_uri must not be blank.\",\"key\":\"docker_uri\"}," +
                "  {\"message\":\"auto_register_timeout must not be blank.\",\"key\":\"auto_register_timeout\"}" +
                "]\n";

        JSONAssert.assertEquals(expectedStr, json, JSONCompareMode.NON_EXTENSIBLE);
    }
}

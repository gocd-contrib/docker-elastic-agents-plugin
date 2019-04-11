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

import cd.go.contrib.elasticagents.docker.requests.ClusterProfileValidateRequest;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Collections;

public class ClusterProfilePropertiesValidateRequestExecutorTest {
    @Test
    public void shouldBarfWhenUnknownKeysArePassed() throws Exception {
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(Collections.singletonMap("foo", "bar")));
        String json = executor.execute().responseBody();
        String expectedStr = "[" +
                "  {\"message\":\"Go Server URL must not be blank.\",\"key\":\"GoServerUrl\"}," +
                "  {\"message\":\"MaxDockerContainers must not be blank.\",\"key\":\"MaxDockerContainers\"}," +
                "  {\"message\":\"DockerUri must not be blank.\",\"key\":\"DockerUri\"}," +
                "  {\"message\":\"AutoRegisterTimeout must not be blank.\",\"key\":\"AutoRegisterTimeout\"}," +
                "  {\"key\":\"foo\",\"message\":\"Is an unknown property\"}" +
                "]";
        JSONAssert.assertEquals(expectedStr, json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateMandatoryKeys() throws Exception {
        ClusterProfileValidateRequestExecutor executor = new ClusterProfileValidateRequestExecutor(new ClusterProfileValidateRequest(Collections.<String, String>emptyMap()));
        String json = executor.execute().responseBody();

        String expectedStr = "[" +
                "  {\"message\":\"Go Server URL must not be blank.\",\"key\":\"GoServerUrl\"}," +
                "  {\"message\":\"MaxDockerContainers must not be blank.\",\"key\":\"MaxDockerContainers\"}," +
                "  {\"message\":\"DockerUri must not be blank.\",\"key\":\"DockerUri\"}," +
                "  {\"message\":\"AutoRegisterTimeout must not be blank.\",\"key\":\"AutoRegisterTimeout\"}" +
                "]\n";

        JSONAssert.assertEquals(expectedStr, json, JSONCompareMode.NON_EXTENSIBLE);
    }
}

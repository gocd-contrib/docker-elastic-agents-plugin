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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.lang.reflect.Type;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetClusterProfilePropertiesMetadataExecutorTest {

    @Test
    public void shouldSerializeAllFields() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileMetadataExecutor().execute();
        final Type type = new TypeToken<List<Metadata>>() {
        }.getType();

        List<Metadata> list = new Gson().fromJson(response.responseBody(), type);
        assertEquals(list.size(), GetClusterProfileMetadataExecutor.FIELDS.size());
    }

    @Test
    public void assertJsonStructure() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileMetadataExecutor().execute();

        assertThat(response.responseCode(), is(200));
        String expectedJSON = "[" +
                "  {" +
                "      \"key\":\"GoServerUrl\"," +
                "      \"metadata\":{\"required\":true,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"EnvironmentVariables\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"MaxDockerContainers\"," +
                "      \"metadata\":{\"required\":true,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"DockerUri\"," +
                "      \"metadata\":{\"required\":true,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"AutoRegisterTimeout\"," +
                "      \"metadata\":{\"required\":true,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"DockerCaCert\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"DockerClientKey\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"DockerClientCert\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"EnablePrivateRegistryAuthentication\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"PrivateRegistryServer\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"PrivateRegistryCustomCredentials\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"PrivateRegistryUsername\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }," +
                "  {" +
                "      \"key\":\"PrivateRegistryPassword\"," +
                "      \"metadata\":{\"required\":false,\"secure\":true}" +
                "  }," +
                "  {" +
                "      \"key\":\"PullOnContainerCreate\"," +
                "      \"metadata\":{\"required\":false,\"secure\":false}" +
                "  }" +
                "]\n";

        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}

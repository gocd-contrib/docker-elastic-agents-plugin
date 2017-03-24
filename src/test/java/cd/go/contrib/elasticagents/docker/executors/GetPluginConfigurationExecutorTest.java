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

package cd.go.contrib.elasticagents.docker.executors;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetPluginConfigurationExecutorTest {

    @Test
    public void shouldSerializeAllFields() throws Exception {
        GoPluginApiResponse response = new GetPluginConfigurationExecutor().execute();
        HashMap hashMap = new Gson().fromJson(response.responseBody(), HashMap.class);
        assertEquals(hashMap.size(), GetPluginConfigurationExecutor.FIELDS.size());
    }

    @Test
    public void assertJsonStructure() throws Exception {
        GoPluginApiResponse response = new GetPluginConfigurationExecutor().execute();

        assertThat(response.responseCode(), is(200));
        String expectedJSON = "{\n" +
                "  \"go_server_url\": {\n" +
                "    \"display-name\": \"Go Server URL\",\n" +
                "    \"required\": true,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"0\"\n" +
                "  },\n" +
                "  \"environment_variables\": {\n" +
                "    \"display-name\": \"Environment Variables\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"1\"\n" +
                "  },\n" +
                "  \"max_docker_containers\": {\n" +
                "    \"display-name\": \"Maximum containers to allow\",\n" +
                "    \"required\": true,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"2\"\n" +
                "  },\n" +
                "  \"docker_uri\": {\n" +
                "    \"display-name\": \"Docker URI\",\n" +
                "    \"required\": true,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"3\"\n" +
                "  },\n" +
                "  \"auto_register_timeout\": {\n" +
                "    \"display-name\": \"Agent auto-register Timeout (in minutes)\",\n" +
                "    \"default-value\": \"10\",\n" +
                "    \"required\": true,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"4\"\n" +
                "  },\n" +
                "  \"docker_ca_cert\": {\n" +
                "    \"display-name\": \"Docker CA Certificate\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"5\"\n" +
                "  },\n" +
                "  \"docker_client_key\": {\n" +
                "    \"display-name\": \"Docker Client Key\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"6\"\n" +
                "  },\n" +
                "  \"docker_client_cert\": {\n" +
                "    \"display-name\": \"Docker Client Certificate\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"7\"\n" +
                "  }," +
                "  \"use_docker_auth_info\": {\n" +
                "    \"display-name\": \"Use Private Registry\",\n" +
                "    \"default-value\": \"false\",\n" +
                "    \"required\": true,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"8\"\n" +
                "  }," +
                "  \"private_registry_server\": {\n" +
                "    \"display-name\": \"Private Registry Server\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"9\"\n" +
                "  }," +
                "  \"private_registry_username\": {\n" +
                "    \"display-name\": \"Private Registry Username\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": false,\n" +
                "    \"display-order\": \"10\"\n" +
                "  }," +
                "  \"private_registry_password\": {\n" +
                "    \"display-name\": \"Private Registry Password\",\n" +
                "    \"required\": false,\n" +
                "    \"secure\": true,\n" +
                "    \"display-order\": \"11\"\n" +
                "  }" +
                "}\n";
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}

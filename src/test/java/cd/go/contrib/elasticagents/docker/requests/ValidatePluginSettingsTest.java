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

package cd.go.contrib.elasticagents.docker.requests;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValidatePluginSettingsTest {

    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        String json = "{\n" +
                "  \"plugin-settings\": {\n" +
                "    \"server_url\": {\n" +
                "      \"value\": \"http://localhost\"\n" +
                "    },\n" +
                "    \"username\": {\n" +
                "      \"value\": \"bob\"\n" +
                "    },\n" +
                "    \"password\": {\n" +
                "      \"value\": \"secret\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        ValidatePluginSettings request = ValidatePluginSettings.fromJSON(json);
        HashMap<String, String> expectedSettings = new HashMap<>();
        expectedSettings.put("server_url", "http://localhost");
        expectedSettings.put("username", "bob");
        expectedSettings.put("password", "secret");
        assertThat(request, equalTo(expectedSettings));
    }
}

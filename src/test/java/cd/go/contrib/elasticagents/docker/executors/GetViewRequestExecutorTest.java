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

import cd.go.contrib.elasticagents.docker.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GetViewRequestExecutorTest {

    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetViewRequestExecutor().execute();
        assertThat(response.responseCode(), is(200));
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), type);
        assertThat(hashSet, hasEntry("template", Util.readResource("/plugin-settings.template.html")));
    }

    @Test
    public void allFieldsShouldBePresentInView() throws Exception {
        String template = Util.readResource("/plugin-settings.template.html");

        for (Map.Entry<String, Field> fieldEntry : GetPluginConfigurationExecutor.FIELDS.entrySet()) {
            assertThat(template, containsString("ng-model=\"" + fieldEntry.getKey() + "\""));
            assertThat(template, containsString("<span class=\"form_error\" ng-show=\"GOINPUTNAME[" + fieldEntry.getKey() +
                    "].$error.server\">{{GOINPUTNAME[" + fieldEntry.getKey() +
                    "].$error.server}}</span>"));
        }
    }

}

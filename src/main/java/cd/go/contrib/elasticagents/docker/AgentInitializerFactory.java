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

package cd.go.contrib.elasticagents.docker;

import com.spotify.docker.client.DockerClient;

public class AgentInitializerFactory {

    public static AgentInitializer create(DockerContainer container, DockerClient docker) {
        if ("false".equals(System.getProperty("rails.use.compressed.js"))) {
            // this is set to false in `DevelopmentServer` from gocd
            return new DevelopmentAgentInitializer(container, docker);
        }

        if ("true".equalsIgnoreCase(System.getProperty("rails.use.compressed.js"))) {
            return new ProductionAgentInitializer(container, docker);
        }

        return AgentInitializer.NULL;
    }
}

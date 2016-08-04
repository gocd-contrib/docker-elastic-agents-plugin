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

import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

public interface AgentInitializer {
    void initialize(String goServerUrl, String autoregisterProperties) throws IOException, DockerException, InterruptedException;

    void startAgent() throws DockerException, InterruptedException;

    AgentInitializer NULL = new AgentInitializer() {
        @Override
        public void initialize(String goServerUrl, String autoregisterProperties) throws IOException, DockerException, InterruptedException {
        }

        @Override
        public void startAgent() throws DockerException, InterruptedException {
        }
    };
}

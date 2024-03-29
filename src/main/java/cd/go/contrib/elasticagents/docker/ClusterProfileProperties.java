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

import java.util.Map;
import java.util.Objects;

public class ClusterProfileProperties extends PluginSettings {
    public static ClusterProfileProperties fromJSON(String json) {
        return GSON.fromJson(json, ClusterProfileProperties.class);
    }

    public static ClusterProfileProperties fromConfiguration(Map<String, String> clusterProfileProperties) {
        return GSON.fromJson(GSON.toJson(clusterProfileProperties), ClusterProfileProperties.class);
    }

    public String uuid() {
        return Integer.toHexString(Objects.hash(this));
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

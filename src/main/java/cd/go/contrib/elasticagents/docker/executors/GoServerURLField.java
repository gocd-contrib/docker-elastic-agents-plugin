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

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GoServerURLField extends Field {

    public GoServerURLField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
    }

    @Override
    public String doValidate(String input) {
        if (isBlank(input)) {
            return this.displayName + " must not be blank.";
        }

        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(input);
        } catch (URISyntaxException e) {
            return this.displayName + " must be a valid URL (https://example.com:8154/go)";
        }

        if (isBlank(uriBuilder.getScheme())) {
            return this.displayName + " must be a valid URL (https://example.com:8154/go)";
        }

        if (!uriBuilder.getScheme().equalsIgnoreCase("https")) {
            return this.displayName + " must be a valid HTTPs URL (https://example.com:8154/go)";
        }

        if (uriBuilder.getHost().equalsIgnoreCase("localhost") || uriBuilder.getHost().equalsIgnoreCase("127.0.0.1")) {
            return this.displayName + " must not be localhost, since this gets resolved on the agents";
        }
        return null;
    }

}

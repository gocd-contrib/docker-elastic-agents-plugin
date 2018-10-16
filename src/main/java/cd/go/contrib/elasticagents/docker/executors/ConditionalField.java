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

import cd.go.contrib.elasticagents.docker.requests.ValidatePluginSettings;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ConditionalField extends Field {

    private Predicate<ValidatePluginSettings> Condition;

    public ConditionalField(String key, String displayName, String defaultValue, Boolean required,
                            Boolean secure, String displayOrder, Predicate<ValidatePluginSettings> condition) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
        this.Condition = condition;
    }

    @Override
    public Map<String, String> validate(ValidatePluginSettings settings) {
        String input = settings.get(key);
        if (StringUtils.isNotBlank(input) && Condition.apply(settings))
        {
            return super.validate(settings);
        }
        return new HashMap<>();
    }

}

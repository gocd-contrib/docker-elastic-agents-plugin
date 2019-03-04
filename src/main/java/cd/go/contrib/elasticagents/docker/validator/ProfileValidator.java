package cd.go.contrib.elasticagents.docker.validator;

import java.util.Map;

/**
 * May validate relationships among profile fields.
 */
public interface ProfileValidator {
    /**
     * Do a validation.
     *
     * @param elasticProfile map of all profile fields and theirs values
     * @return map of errors
     */
    Map<String, String> validate(Map<String, String> elasticProfile);
}

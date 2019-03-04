package cd.go.contrib.elasticagents.docker.validator;

import cd.go.contrib.elasticagents.docker.MemorySpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.docker.executors.GetProfileMetadataExecutor.MAX_MEMORY;
import static cd.go.contrib.elasticagents.docker.executors.GetProfileMetadataExecutor.RESERVED_MEMORY;

/**
 * Check that max memory is not lower than reserved memory.
 */
public class MemorySettingsProfileValidator implements ProfileValidator {
    @Override
    public Map<String, String> validate(Map<String, String> elasticProfile) {
        final List<String> fieldErrors = new ArrayList<>();
        String maxMemoryInput = elasticProfile.get(MAX_MEMORY.getKey());
        String reservedMemoryInput = elasticProfile.get(RESERVED_MEMORY.getKey());

        Long maxMemory = MemorySpecification.parse(maxMemoryInput, fieldErrors);
        Long reservedMemory = MemorySpecification.parse(reservedMemoryInput, fieldErrors);

        Map<String, String> errors = new HashMap<>();

        if (fieldErrors.isEmpty()) {
            if (maxMemory != null && maxMemory < 4 * 1024 * 1024) {
                // this is docker limitation,
                // see https://docs.docker.com/config/containers/resource_constraints/#memory
                addError(errors, MAX_MEMORY.getKey(), "Minimum allowed value is 4M");
            } else if (maxMemory != null && reservedMemory != null && maxMemory < reservedMemory) {
                addError(errors, MAX_MEMORY.getKey(), "Max memory is lower than reserved memory");
            }
        }

        return errors;
    }

    private void addError(Map<String, String> errors, String key, String message) {
        errors.put("key", key);
        errors.put("message", message);
    }
}

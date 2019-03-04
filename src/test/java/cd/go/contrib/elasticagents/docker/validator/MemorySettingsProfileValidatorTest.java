package cd.go.contrib.elasticagents.docker.validator;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagents.docker.executors.GetProfileMetadataExecutor.MAX_MEMORY;
import static cd.go.contrib.elasticagents.docker.executors.GetProfileMetadataExecutor.RESERVED_MEMORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MemorySettingsProfileValidatorTest {
    @Test
    public void minimumMaxMemoryValue() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY.getKey(), "3M");

        Map<String, String> errors = new MemorySettingsProfileValidator().validate(elasticProfile);

        assertEquals(MAX_MEMORY.getKey(), errors.get("key"));
        assertEquals("Minimum allowed value is 4M", errors.get("message"));
    }

    @Test
    public void bothMemorySettingsAreEmpty() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY.getKey(), "");
        elasticProfile.put(RESERVED_MEMORY.getKey(), "");

        Map<String, String> errors = new MemorySettingsProfileValidator().validate(elasticProfile);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void maxMemorySetReservedMemoryEmpty() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY.getKey(), "2G");
        elasticProfile.put(RESERVED_MEMORY.getKey(), "");

        Map<String, String> errors = new MemorySettingsProfileValidator().validate(elasticProfile);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void maxMemoryIsLowerThanReservedMemory() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY.getKey(), "1G");
        elasticProfile.put(RESERVED_MEMORY.getKey(), "2G");

        Map<String, String> errors = new MemorySettingsProfileValidator().validate(elasticProfile);

        assertEquals(MAX_MEMORY.getKey(), errors.get("key"));
        assertEquals("Max memory is lower than reserved memory", errors.get("message"));
    }
}
package cd.go.contrib.elasticagents.docker;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HostMetadataTest {

    @Test
    public void shouldValidateHostConfig() throws Exception {
        final Map<String, String> validate = new HostMetadata().validate("10.0.0.1 hostname-1 hostname-2");

        assertTrue(validate.isEmpty());
    }

    @Test
    public void shouldValidateIPAddress() throws Exception {
        final Map<String, String> validationResult = new HostMetadata().validate("10.0.0.foo hostname");

        assertThat(validationResult.size(), is(2));
        assertThat(validationResult, hasEntry("message", "'10.0.0.foo' is not an IP string literal."));
        assertThat(validationResult, hasEntry("key", "Hosts"));
    }

    @Test
    public void shouldValidateInvalidHostConfig() throws Exception {
        Map<String, String> validationResult = new HostMetadata().validate("some-config");

        assertThat(validationResult.size(), is(2));
        assertThat(validationResult, hasEntry("message", "Host entry `some-config` is invalid."));
        assertThat(validationResult, hasEntry("key", "Hosts"));
    }
}
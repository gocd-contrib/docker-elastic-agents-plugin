package cd.go.contrib.elasticagents.docker;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class HostsTest {

    @Test
    public void shouldReturnEmptyListWhenHostConfigIsNotProvided() throws Exception {
        assertThat(new Hosts(null), hasSize(0));
        assertThat(new Hosts(""), hasSize(0));
    }

    @Test
    public void shouldReturnHostMappingForOneIpToOneHostnameMapping() throws Exception {
        final Hosts hosts = new Hosts("10.0.0.1 foo-host");

        assertThat(hosts, hasSize(1));
        assertThat(hosts, hasItem("foo-host:10.0.0.1"));
    }

    @Test
    public void shouldReturnHostMappingForOneIpToMAnyHostnameMapping() throws Exception {
        final Hosts hosts = new Hosts("10.0.0.1 foo-host bar-host");

        assertThat(hosts, hasSize(1));
        assertThat(hosts, hasItem("foo-host bar-host:10.0.0.1"));
    }

    @Test
    public void shouldIgnoreEmptyLines() throws Exception {
        final Hosts hosts = new Hosts("10.0.0.1 foo-host\n\n\n 10.0.0.2 bar-host");

        assertThat(hosts, hasSize(2));
        assertThat(hosts, hasItem("foo-host:10.0.0.1"));
        assertThat(hosts, hasItem("bar-host:10.0.0.2"));
    }

    @Test
    public void shouldValidateHostConfig() throws Exception {
        final Hosts hosts = new Hosts("10.0.0.foo foo-host\n bar-host\n 10.0.0.1 baz-host");

        assertThat(hosts, hasSize(1));
        assertThat(hosts, hasItem("baz-host:10.0.0.1"));

        assertThat(hosts.getErrors(), hasSize(2));
        assertThat(hosts.getErrors(), containsInAnyOrder(
                "'10.0.0.foo' is not an IP string literal.",
                "Host entry `bar-host` is invalid.")
        );
    }
}
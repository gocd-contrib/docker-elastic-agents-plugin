package cd.go.contrib.elasticagents.docker.requests;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServerPingRequestTest {
    @Test
    public void shouldDeserializeJSONBody() {
        String requestBody = """
                {
                 "all_cluster_profile_properties": [
                   {
                      "go_server_url": "foo",
                      "max_docker_containers": "100",
                      "docker_uri": "dockerURI",
                      "auto_register_timeout": "1",
                      "private_registry_password": "foobar",
                      "enable_private_registry_authentication": "false",
                      "private_registry_custom_credentials": "true",
                      "pull_on_container_create": "false"
                    }
                  ]
                }""";

        HashMap<String, String> configurations = new HashMap<>();
        configurations.put("go_server_url", "foo");
        configurations.put("max_docker_containers", "100");
        configurations.put("docker_uri", "dockerURI");
        configurations.put("auto_register_timeout", "1");
        configurations.put("private_registry_password", "foobar");
        configurations.put("enable_private_registry_authentication", "false");
        configurations.put("private_registry_custom_credentials", "true");
        configurations.put("pull_on_container_create", "false");
        assertThat(ServerPingRequest.fromJSON(requestBody), is(new ServerPingRequest(Arrays.asList(configurations))));
    }
}

package cd.go.contrib.elasticagents.docker.requests;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServerPingRequestTest {
    @Test
    public void shouldDeserializeJSONBody() {
        String requestBody = "{\n " +
                " \"all_cluster_profile_properties\": [\n    " +
                "{\n      " +
                "      \"go_server_url\": \"foo\",\n" +
                "      \"max_docker_containers\": \"100\",\n" +
                "      \"docker_uri\": \"dockerURI\",\n" +
                "      \"auto_register_timeout\": \"1\",\n" +
                "      \"private_registry_password\": \"foobar\",\n" +
                "      \"enable_private_registry_authentication\": \"false\",\n" +
                "      \"private_registry_custom_credentials\": \"true\",\n" +
                "      \"pull_on_container_create\": \"false\"\n" +
                "    }\n" +
                "   ]" +
                "\n}";

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

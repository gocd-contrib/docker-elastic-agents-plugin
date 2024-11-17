package cd.go.contrib.elasticagents.docker;

import com.spotify.docker.client.messages.Network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworksTest {

    @Test
    public void shouldReturnNullWhenNetworkConfigIsNotProvided() {
        assertNull(Networks.fromString(null, Collections.emptyList()));
        assertNull(Networks.fromString("", Collections.emptyList()));
    }

    @Test
    public void shouldReturnFirstNetworkFromString() {
        Network network = mock(Network.class);
        when(network.name()).thenReturn("gocd-net");
        when(network.id()).thenReturn("network-id-1");

        List<Network> networks = new ArrayList<>();
        networks.add(network);

        String result = Networks.fromString("gocd-net", networks);

        assertNotNull(result);
        assertEquals("gocd-net", result);
    }

    @Test
    public void shouldThrowExceptionWhenNetworkDoesNotExist() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Networks.fromString("gocd-net", Collections.emptyList());
        });

        assertEquals("Network with name `gocd-net` does not exist.", exception.getMessage());
    }

    @Test
    public void shouldReturnEmptyListForAdditionalNetworksWhenOnlyOneNetwork() {
        assertTrue(Networks.getAdditionalNetworks("single-network").isEmpty());
    }

    @Test
    public void shouldReturnAdditionalNetworksWhenMultipleNetworksProvided() {
        String networkConfig = "network1\nnetwork2\nnetwork3";
        var additionalNetworks = Networks.getAdditionalNetworks(networkConfig);

        assertEquals(2, additionalNetworks.size());
        assertTrue(additionalNetworks.contains("network2"));
        assertTrue(additionalNetworks.contains("network3"));
    }
}

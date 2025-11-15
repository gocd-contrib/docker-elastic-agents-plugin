package cd.go.contrib.elasticagents.docker;

import com.spotify.docker.client.messages.Network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.docker.utils.Util.splitIntoLinesAndTrimSpaces;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Networks {

    private Networks() {}

    public static String firstMatching(String networkConfig, List<Network> dockerNetworks) {
        if (isBlank(networkConfig)) {
            return null;
        }

        final List<String> networkEntries = splitIntoLinesAndTrimSpaces(networkConfig);

        final Collection<String> missingNetworks = networkEntries
                .stream()
                .filter(networkEntry -> dockerNetworks.stream().noneMatch(network -> network.name().equals(networkEntry)))
                .collect(Collectors.toList());

        if (!missingNetworks.isEmpty()) {
            throw new RuntimeException(String.format("Networks %s do not exist.", missingNetworks));
        }

        return networkEntries.get(0);
    }

    public static Collection<String> getAdditionalNetworks(String networkConfig) {
        if (isBlank(networkConfig)) {
            return Collections.emptyList();
        }

        final List<String> networkEntries = splitIntoLinesAndTrimSpaces(networkConfig);

        return networkEntries.isEmpty() ? networkEntries : networkEntries.subList(1, networkEntries.size());
    }
}

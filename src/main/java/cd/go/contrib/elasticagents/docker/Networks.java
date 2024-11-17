package cd.go.contrib.elasticagents.docker;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static cd.go.contrib.elasticagents.docker.utils.Util.splitIntoLinesAndTrimSpaces;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.spotify.docker.client.messages.Network;

public class Networks {
   public static String fromString(String networkConfig, List<Network> dockerNetworks) {
       if (isBlank(networkConfig)) {
           return null;
       }

       final Map<String, Network> availableNetworks = dockerNetworks.stream()
               .collect(Collectors.toMap(Network::name, network -> network));

       final Collection<String> networkEntries = splitIntoLinesAndTrimSpaces(networkConfig);
       if (networkEntries.isEmpty()) {
           return null;
       }

       networkEntries.forEach(networkEntry -> {
           final Network availableNetwork = availableNetworks.get(networkEntry);
           if (availableNetwork == null) {
               throw new RuntimeException(format("Network with name `{0}` does not exist.", networkEntry));
           }
           LOG.debug(format("Using network `{0}` with id `{1}`.", networkEntry, availableNetwork.id()));
       });

       return networkEntries.iterator().next();
   }

   public static Collection<String> getAdditionalNetworks(String networkConfig) {
       if (isBlank(networkConfig)) {
           return Collections.emptyList();
       }

       final Collection<String> networkEntries = splitIntoLinesAndTrimSpaces(networkConfig);
       if (networkEntries.size() <= 1) {
           return Collections.emptyList();
       }

       List<String> additionalNetworks = new ArrayList<>(networkEntries);
       additionalNetworks.remove(additionalNetworks.iterator().next());
       return additionalNetworks;
   }
}

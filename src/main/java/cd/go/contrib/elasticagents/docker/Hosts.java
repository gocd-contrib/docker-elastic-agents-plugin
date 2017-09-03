package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.utils.Util;
import com.google.common.net.InetAddresses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class Hosts extends ArrayList<String> {
    private List<String> errors = new ArrayList<>();

    public Hosts(String hostConfig) {
        final Collection<String> hostEntries = Util.splitIntoLinesAndTrimSpaces(hostConfig);

        for (String hostEntry : hostEntries) {
            if (validate(hostEntry)) {
                String[] parts = hostEntry.split("\\s+", 2);
                add(trimToEmpty(parts[1]) + ":" + trimToEmpty(parts[0]));
            }
        }
    }

    private boolean validate(String hostEntry) {
        String[] parts = hostEntry.split("\\s+", 2);
        if (parts.length != 2) {
            this.errors.add(format("Host entry `{0}` is invalid.", hostEntry));
            return false;
        }

        if (validIPAddress(parts[0])) {
            return true;
        }

        return false;
    }

    private boolean validIPAddress(String ipAddress) {
        try {
            InetAddresses.forString(ipAddress);
            return true;
        } catch (Exception e) {
            this.errors.add(e.getMessage());
        }

        return false;
    }

    public List<String> getErrors() {
        return errors;
    }
}

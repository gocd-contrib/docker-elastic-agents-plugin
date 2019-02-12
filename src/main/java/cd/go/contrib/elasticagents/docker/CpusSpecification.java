package cd.go.contrib.elasticagents.docker;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse Cpus value, expected format
 * <ul>
 * <li>float number</li>
 * <li>decimal separator: a dot (.)</li>
 * </ul>
 * The "cpus" settings is not allowed by the Spotify docker client yet.
 * Once the client supports the parameter, this can be simplified.
 * Currently, the "cpus" value is translated as it is written in documentation:
 * <tt>--cpus="1.5"</tt> is equivalent of setting <tt>--cpu-period="100000"</tt>
 * and <tt>--cpu-quota="150000"</tt>.
 *
 * @see <a href="https://docs.docker.com/config/containers/resource_constraints/#cpu">Docker CPU settings</a>.
 */
public class CpusSpecification {
    private List<String> errors = new ArrayList<>();
    private Float cpus;

    public CpusSpecification(String cpus) {
        this.cpus = parse(cpus, errors);
    }

    public Float getCpus() {
        return cpus;
    }

    public long getCpuPeriod() {
        return 100_000l;
    }

    public long getCpuQuota() {
        return ((long) (getCpus() * getCpuPeriod()));
    }

    public List<String> getErrors() {
        return errors;
    }

    public static Float parse(String cpus, List<String> errors) {
        if (StringUtils.isBlank(cpus)) {
            return null;
        }

        try {
            return Float.parseFloat(cpus);
        } catch (NumberFormatException e) {
            errors.add("Invalid float number: " + cpus);
            return null;
        }
    }
}

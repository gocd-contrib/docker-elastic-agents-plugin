package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.CpusSpecification;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Accept CPUS value (<tt>docker --cpus</tt>) and transform it to <tt>--cpu-period</tt> and <tt>--cpu-quota</tt>
 * parameters until following issue is solved:
 * <br>
 *
 * <a href="https://github.com/spotify/docker-client/issues/959">Support host config cpus</a>
 */
public class CpusMetadata extends Metadata {
    public CpusMetadata(String key) {
        super(key);
    }

    @Override
    protected String doValidate(String input) {
        final List<String> errors = new ArrayList<>();
        final String doValidateResult = super.doValidate(input);

        if (isNotBlank(doValidateResult)) {
            errors.add(doValidateResult);
        }

        CpusSpecification.parse(input, errors);

        return errors.isEmpty() ? null : StringUtils.join(errors, ". ");
    }
}

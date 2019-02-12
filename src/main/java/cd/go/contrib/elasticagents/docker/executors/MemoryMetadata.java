package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.MemorySpecification;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Memory specification: number followed by M (megabytes), G (gigabytes), T (terabytes)
 */
public class MemoryMetadata extends Metadata {
    MemoryMetadata(String key) {
        super(key);
    }

    @Override
    protected String doValidate(String input) {
        final List<String> errors = new ArrayList<>();
        final String doValidateResult = super.doValidate(input);

        if (isNotBlank(doValidateResult)) {
            errors.add(doValidateResult);
        }

        MemorySpecification.parse(input, errors);

        return errors.isEmpty() ? null : StringUtils.join(errors, ". ");
    }
}

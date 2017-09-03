package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.executors.Metadata;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class HostMetadata extends Metadata {
    public HostMetadata() {
        super("Hosts", false, false);
    }

    @Override
    protected String doValidate(String input) {
        final List<String> errors = new ArrayList<>();
        final String doValidateResult = super.doValidate(input);

        if (isNotBlank(doValidateResult)) {
            errors.add(doValidateResult);
        }

        errors.addAll(new Hosts(input).getErrors());

        return errors.isEmpty() ? null : StringUtils.join(errors, ". ");
    }
}
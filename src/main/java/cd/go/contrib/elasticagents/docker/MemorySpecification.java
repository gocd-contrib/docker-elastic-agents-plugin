package cd.go.contrib.elasticagents.docker;

import com.google.common.collect.ImmutableSortedMap;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse memory specification. Expected format is:
 * <ul>
 *     <li>a float number</li>
 *     <li>followed by a letter: M, G, T</li>
 * </ul>
 */
public class MemorySpecification {
    private static final Pattern PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)(\\D)");

    private static final Map<String, BigDecimal> SUFFIXES = ImmutableSortedMap.<String, BigDecimal>orderedBy(java.lang.String.CASE_INSENSITIVE_ORDER)
            .put("M", BigDecimal.valueOf(1024L * 1024L))
            .put("G", BigDecimal.valueOf(1024L * 1024L * 1024L))
            .put("T", BigDecimal.valueOf(1024L * 1024L * 1024L * 1024L))
            .build();

    private List<String> errors = new ArrayList<>();
    private Long memory;

    MemorySpecification(String memory) {
        this.memory = parse(memory, errors);
    }

    Long getMemory() {
        return memory;
    }

    public static Long parse(String memory, List<String> errors) {
        if (StringUtils.isBlank(memory)) {
            return null;
        }

        final Matcher matcher = PATTERN.matcher(memory);
        if (!matcher.matches()) {
            errors.add("Invalid size: " + memory);
            return null;
        }

        final BigDecimal size = new BigDecimal(matcher.group(1));
        final BigDecimal unit = SUFFIXES.get(matcher.group(3));
        if (unit == null) {
            errors.add("Invalid size: " + memory + ". Wrong size unit");
            return null;
        }

        return size.multiply(unit).longValue();
    }

    public List<String> getErrors() {
        return errors;
    }
}

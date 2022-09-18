package cd.go.contrib.elasticagents.docker;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CpusSpecificationTest {
    @Test
    public void cpusIsTranslatedToCpuPeriodAndCpuQuota() {
        CpusSpecification cpus1 = new CpusSpecification("1.5");

        assertEquals(100_000L, cpus1.getCpuPeriod());
        assertEquals(150_000L, cpus1.getCpuQuota());

        CpusSpecification cpus2 = new CpusSpecification(".5");

        assertEquals(100_000L, cpus2.getCpuPeriod());
        assertEquals(50_000L, cpus2.getCpuQuota());
    }

    @Test
    public void cpusParsedWithErrors() {
        assertEquals(Collections.singletonList("Invalid float number: 0,3"), new CpusSpecification("0,3").getErrors());
        assertEquals(Collections.singletonList("Invalid float number: abc"), new CpusSpecification("abc").getErrors());
    }
}
package cd.go.contrib.elasticagents.docker;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MemorySpecificationTest {
    @Test
    public void memorySpecificationIsParsedCorrectly() {
        assertEquals(Long.valueOf(10L * 1024L * 1024L), new MemorySpecification("10M").getMemory());
        assertEquals(Long.valueOf(25L * 1024L * 1024L * 1024L), new MemorySpecification("25G").getMemory());
        assertEquals(Long.valueOf(138L * 1024L * 1024L * 1024L * 1024L), new MemorySpecification("138T").getMemory());
        assertEquals(Long.valueOf(15L * 1024L * 1024L / 10L), new MemorySpecification("1.5M").getMemory());
    }

    @Test
    public void memorySpecificationParsingErrors() {
        assertEquals(Collections.singletonList("Invalid size: 5K. Wrong size unit"), new MemorySpecification("5K").getErrors());
        assertEquals(Collections.singletonList("Invalid size: 5"), new MemorySpecification("5").getErrors());
        assertEquals(Collections.singletonList("Invalid size: A"), new MemorySpecification("A").getErrors());
        assertEquals(Collections.singletonList("Invalid size: .3M"), new MemorySpecification(".3M").getErrors());
        assertEquals(Collections.singletonList("Invalid size: 1,3M"), new MemorySpecification("1,3M").getErrors());
    }
}
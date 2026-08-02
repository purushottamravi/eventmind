package com.ravi.eventmind.observability.jfr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the JFR analyzer turns the collected numbers into a readable report, and shows n/a when there's no data to talk about.
 */
class JfrAnalyzerTest {

    @Test
    void analyze_shouldRenderCollectedSignal() {
        JfrReport report = new JfrReport();
        report.recordGc(50, "Allocation Failure");
        report.recordCpuLoad(20f, 5f, 3f);
        report.recordMethodExecution(300, "Foo", "slow", true);
        report.recordException("java.lang.OutOfMemoryError");
        report.recordException("java.lang.NullPointerException");

        String result = new JfrAnalyzer(report).analyze();

        assertTrue(result.startsWith("JFR REPORT"));
        assertTrue(result.contains("collections: 1"));
        assertTrue(result.contains("total pause: 50 ms"));
        assertTrue(result.contains("max pause: 50 ms (Allocation Failure)"));
        assertTrue(result.contains("machine total: 20.0%"));
        assertTrue(result.contains("jvm: 8.0%"));
        assertTrue(result.contains("total: 1"));
        assertTrue(result.contains("failures: 1"));
        assertTrue(result.contains("slowest: 300 ms (Foo.slow)"));
        assertTrue(result.contains("java.lang.OutOfMemoryError: 1"));
        assertTrue(result.contains("java.lang.NullPointerException: 1"));
    }

    @Test
    void analyze_shouldRenderNaWhenNoCpuSampleCollected() {
        JfrReport report = new JfrReport();

        String result = new JfrAnalyzer(report).analyze();

        assertTrue(result.contains("machine total: n/a"));
        assertTrue(result.contains("jvm: n/a"));
        assertFalse(result.contains("by class:"));
    }
}

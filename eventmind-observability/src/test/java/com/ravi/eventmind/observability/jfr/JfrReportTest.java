package com.ravi.eventmind.observability.jfr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JfrReportTest {

    @Test
    void snapshot_shouldReflectRecordedEvents() {
        JfrReport report = new JfrReport();

        report.recordGc(50, "Allocation Failure");
        report.recordGc(120, "System.gc()");
        report.recordCpuLoad(20f, 5f, 3f);
        report.recordMethodExecution(12, "Foo", "fast", false);
        report.recordMethodExecution(300, "Foo", "slow", true);
        report.recordException("java.lang.OutOfMemoryError");
        report.recordException("java.lang.OutOfMemoryError");
        report.recordException("java.lang.NullPointerException");

        JfrReport.Snapshot s = report.snapshot();

        assertEquals(2, s.gcCount());
        assertEquals(170, s.totalGcPauseMs());
        assertEquals(120, s.maxGcPauseMs());
        assertEquals("System.gc()", s.maxGcCause());

        assertEquals(20f, s.machineCpuLoad());
        assertEquals(8f, s.jvmCpuLoad());

        assertEquals(2, s.methodExecutionCount());
        assertEquals(1, s.methodExecutionFailures());
        assertEquals(300, s.slowestMethodExecutionMs());
        assertEquals("Foo.slow", s.slowestMethodName());

        assertEquals(3, s.exceptionCount());
        assertEquals(2, s.exceptionCountByClass().size());
    }

    @Test
    void snapshot_shouldBeSafeFromFurtherMutations() {
        JfrReport report = new JfrReport();
        report.recordGc(50, "Allocation Failure");
        report.recordException("java.lang.NullPointerException");

        JfrReport.Snapshot before = report.snapshot();

        report.recordGc(10, "System.gc()");
        report.recordException("java.lang.OutOfMemoryError");

        assertEquals(1, before.gcCount());
        assertEquals(1, before.exceptionCount());
    }
}

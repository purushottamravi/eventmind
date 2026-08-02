package com.ravi.eventmind.observability.jfr;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe accumulator of the JVM metrics collected from the live JFR stream.
 *
 * <p>Written to by {@link JfrEventListener} on the recording-stream thread and read
 * by {@link JfrAnalyzer} when a report is requested, so every mutation is
 * synchronized and snapshots are immutable copies.</p>
 */
@Component
public class JfrReport {

    /** Name of the custom event committed by {@code AuditAspect} on {@code @AuditLog} methods. */
    public static final String METHOD_EXECUTION_EVENT = "com.ravi.eventmind.MethodExecution";

    private long gcCount;
    private long totalGcPauseMs;
    private long maxGcPauseMs;
    private String maxGcCause;

    private volatile float machineCpuLoad = -1f;
    private volatile float jvmCpuLoad = -1f;

    private long methodExecutionCount;
    private long methodExecutionFailures;
    private long slowestMethodExecutionMs;
    private String slowestMethodName;

    private long exceptionCount;
    private final Map<String, Long> exceptionCountByClass = new LinkedHashMap<>();

    private final long startedAtMillis = System.currentTimeMillis();

    public synchronized void recordGc(long pauseMs, String cause) {
        gcCount++;
        totalGcPauseMs += pauseMs;
        if (pauseMs > maxGcPauseMs) {
            maxGcPauseMs = pauseMs;
            maxGcCause = cause;
        }
    }

    public void recordCpuLoad(float machineTotal, float jvmUser, float jvmSystem) {
        machineCpuLoad = machineTotal;
        jvmCpuLoad = jvmUser + jvmSystem;
    }

    public synchronized void recordMethodExecution(long executionTimeMs, String className, String methodName, boolean failed) {
        methodExecutionCount++;
        if (failed) {
            methodExecutionFailures++;
        }
        if (executionTimeMs > slowestMethodExecutionMs) {
            slowestMethodExecutionMs = executionTimeMs;
            slowestMethodName = className + "." + methodName;
        }
    }

    public synchronized void recordException(String throwableClass) {
        exceptionCount++;
        exceptionCountByClass.merge(throwableClass, 1L, Long::sum);
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                startedAtMillis,
                gcCount, totalGcPauseMs, maxGcPauseMs, maxGcCause,
                machineCpuLoad, jvmCpuLoad,
                methodExecutionCount, methodExecutionFailures, slowestMethodExecutionMs, slowestMethodName,
                exceptionCount, List.copyOf(exceptionCountByClass.entrySet()));
    }

    /**
     * Immutable point-in-time view of the accumulated metrics.
     */
    public record Snapshot(
            long startedAtMillis,
            long gcCount, long totalGcPauseMs, long maxGcPauseMs, String maxGcCause,
            float machineCpuLoad, float jvmCpuLoad,
            long methodExecutionCount, long methodExecutionFailures,
            long slowestMethodExecutionMs, String slowestMethodName,
            long exceptionCount, List<Map.Entry<String, Long>> exceptionCountByClass) {
    }
}

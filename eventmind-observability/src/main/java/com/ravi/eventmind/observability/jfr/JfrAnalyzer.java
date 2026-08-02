package com.ravi.eventmind.observability.jfr;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Renders the JVM signal collected by {@link JfrEventListener} into a human-readable
 * report that can be passed to the AI module as the {@code jfrReport} analysis input.
 */
@Service
public class JfrAnalyzer {

    private final JfrReport report;

    public JfrAnalyzer(JfrReport report) {
        this.report = report;
    }

    public String analyze() {
        JfrReport.Snapshot s = report.snapshot();
        StringBuilder sb = new StringBuilder();
        sb.append("JFR REPORT (generated ").append(DateTimeFormatter.ISO_INSTANT.format(Instant.now())).append(")\n");
        sb.append("Recording started at: ")
                .append(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(s.startedAtMillis())))
                .append("\n");

        sb.append("GC:\n");
        sb.append("  collections: ").append(s.gcCount()).append("\n");
        sb.append("  total pause: ").append(s.totalGcPauseMs()).append(" ms\n");
        sb.append("  max pause: ").append(s.maxGcPauseMs()).append(" ms");
        if (s.maxGcCause() != null) {
            sb.append(" (").append(s.maxGcCause()).append(")");
        }
        sb.append("\n");

        sb.append("CPU:\n");
        sb.append("  machine total: ").append(formatCpu(s.machineCpuLoad())).append("\n");
        sb.append("  jvm: ").append(formatCpu(s.jvmCpuLoad())).append("\n");

        sb.append("Method executions:\n");
        sb.append("  total: ").append(s.methodExecutionCount()).append("\n");
        sb.append("  failures: ").append(s.methodExecutionFailures()).append("\n");
        sb.append("  slowest: ").append(s.slowestMethodExecutionMs()).append(" ms");
        if (s.slowestMethodName() != null) {
            sb.append(" (").append(s.slowestMethodName()).append(")");
        }
        sb.append("\n");

        sb.append("Exceptions:\n");
        sb.append("  total: ").append(s.exceptionCount()).append("\n");
        if (!s.exceptionCountByClass().isEmpty()) {
            sb.append("  by class:\n");
            for (Map.Entry<String, Long> entry : s.exceptionCountByClass()) {
                sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }

    private String formatCpu(float value) {
        if (value < 0) {
            return "n/a";
        }
        return String.format("%.1f%%", value);
    }
}

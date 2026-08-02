package com.ravi.eventmind.ai.model;

/**
 * Bundles the symptom, logs, and JFR report into one thing we can hand to the AI.
 * Nothing but fields.
 */
public record DiagnosticContext(String symptom, String logs, String jfrReport) {
}

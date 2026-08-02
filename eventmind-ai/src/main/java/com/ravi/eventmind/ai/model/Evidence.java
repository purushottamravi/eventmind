package com.ravi.eventmind.ai.model;

/**
 * One snippet of proof that backs a recommendation, with its source and severity.
 * Pure data.
 */
public record Evidence(String source, String finding, String severity) {
}

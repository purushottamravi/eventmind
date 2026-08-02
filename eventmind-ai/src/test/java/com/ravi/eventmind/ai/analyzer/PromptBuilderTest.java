package com.ravi.eventmind.ai.analyzer;

import com.ravi.eventmind.ai.model.DiagnosticContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void build_shouldIncludeSymptomLogsAndJfr() {
        String prompt = promptBuilder.build(new DiagnosticContext("OutOfMemory", "ERROR java.lang.OutOfMemoryError", "GC paused 10s"));

        assertTrue(prompt.contains("OutOfMemory"));
        assertTrue(prompt.contains("ERROR java.lang.OutOfMemoryError"));
        assertTrue(prompt.contains("GC paused 10s"));
    }

    @Test
    void build_shouldInjectKnowledgeContext() {
        String prompt = promptBuilder.build(new DiagnosticContext("OOM", "logs", "jfr"), "Heap exhaustion is the usual cause of OOM");

        assertTrue(prompt.contains("Heap exhaustion is the usual cause of OOM"));
    }

    @Test
    void build_shouldWorkWithoutKnowledgeContext() {
        String prompt = promptBuilder.build(new DiagnosticContext("OOM", "logs", "jfr"));

        assertTrue(prompt.contains("OOM"));
    }
}

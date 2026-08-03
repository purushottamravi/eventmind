package com.ravi.eventmind.ai.analyzer;

import com.ravi.eventmind.ai.model.DiagnosticContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the prompt builder stuffs symptoms, logs, JFR, and knowledge context into the prompt.
 * So we don't accidentally ship a prompt that forgets the user's problem.
 */
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

    @Test
    void build_shouldUseFieldNamesTheDeserializerActuallyReads() {
        String prompt = promptBuilder.build(new DiagnosticContext("OOM", "logs", "jfr"));

        assertTrue(prompt.contains("\"suggestedAction\""));
        assertTrue(prompt.contains("\"source\""));
        assertTrue(prompt.contains("\"finding\""));
        assertTrue(!prompt.contains("recommendedAction"));
    }
}

package com.ravi.eventmind.ai.analyzer;


import com.ravi.eventmind.ai.model.DiagnosticContext;
import org.springframework.stereotype.Component;

/**
 * Rolls the symptom, logs, and JFR report into one big prompt for the LLM.
 * Does the formatting so the model gets a clean, consistent ask every time.
 */
@Component
public class PromptBuilder {
    public String build(DiagnosticContext context) {
        return build(context, "");
    }

    public String build(DiagnosticContext context, String knowledgeContext) {

        return """
        ROLE:
        You are an expert Java Site Reliability Engineer (SRE)
        specializing in Spring Boot, CQRS, Event Sourcing,
        JVM performance analysis, and production troubleshooting.


        OBJECTIVE:
        Analyze the provided application symptom, logs, and JVM
        diagnostic information.

        Your task is to identify:
        - Possible root cause
        - Impact of the problem
        - Recommended recovery action
        - Confidence level of your analysis


        APPLICATION CONTEXT:

        Application:
        EventMind

        Architecture:
        - Spring Boot
        - CQRS
        - Event Sourcing
        - Axon Framework

        Diagnostic sources:
        - Application logs
        - Symptom information
        - Java Flight Recorder analysis


        IMPORTANT RULES:

        1. Do not execute any action.
        2. Do not modify application state.
        3. Only provide recommendations.
        4. Every recommendation requires human approval.
        5. If evidence is insufficient, state that clearly.
        6. Do not invent information that is not present in the evidence.


        KNOWLEDGE BASE:
        The following documents were retrieved from the operational
        knowledge base. Use them only if they are relevant to the
        symptom. If the section is empty or irrelevant, ignore it.

        %s


        SYMPTOM:

        %s


        APPLICATION LOGS:

        %s


        JVM/JFR ANALYSIS:

        %s


        RESPONSE FORMAT:

        Return a single JSON object only. No markdown code fences,
        no commentary, no trailing text.

        {
          "problem": "",
          "severity": "",
          "rootCause": "",
          "evidence": [
            {
              "source": "",
              "finding": "",
              "severity": ""
            }
          ],
          "suggestedAction": "",
          "confidence": 0,
          "requiresHumanApproval": true
        }

        suggestedAction MUST be a concrete, non-empty action string
        (for example "Clear the application cache" or "Restart the
        affected service"). Never leave it empty.

        """
                .formatted(
                        knowledgeContext,
                        context.symptom(),
                        context.logs(),
                        context.jfrReport()
                );
    }
}
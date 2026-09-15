package com.ravi.eventmind.observability.jfr;

import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

/**
 * When a new symptom lands on the Kafka topic, extracts a JFR diagnostic
 * snapshot and pushes it to the AI module for structured healing analysis.
 */
@Component
@ProcessingGroup("observability")
public class JfrSymptomTrigger {

    private final JfrAnalyzer jfrAnalyzer;
    private final HealingAnalysisClient healingAnalysisClient;

    public JfrSymptomTrigger(JfrAnalyzer jfrAnalyzer, HealingAnalysisClient healingAnalysisClient) {
        this.jfrAnalyzer = jfrAnalyzer;
        this.healingAnalysisClient = healingAnalysisClient;
    }

    @EventHandler
    public void on(SymptomCreatedEvent event) {
        String diagnosticReportText = jfrAnalyzer.analyze();

        healingAnalysisClient.analyze(new AiAnalyzeRequest(
                event.id(),
                event.name(),
                diagnosticReportText
        ));
    }
}

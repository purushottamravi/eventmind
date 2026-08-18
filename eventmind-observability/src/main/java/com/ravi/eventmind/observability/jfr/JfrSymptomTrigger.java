package com.ravi.eventmind.observability.jfr;

import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component // <-- Spring manages this bean
public class JfrSymptomTrigger {

    private final JfrAnalyzer jfrAnalyzer;
    // You might also inject your AI CommandGateway here to send the next step!

    public JfrSymptomTrigger(JfrAnalyzer jfrAnalyzer) {
        this.jfrAnalyzer = jfrAnalyzer;
    }

    @EventHandler
    public void on(SymptomCreatedEvent event) {
        log.info("Symptom detected: {}. Kicking off automated JFR analysis stream.", event.name());

        // 1. Generate the textual performance report automatically!
        String diagnosticReport = jfrAnalyzer.analyze();

        log.info("JFR Analysis Report compiled successfully for symptom ID: {}", event.id());

        // 2. Next logical step: Pass this report text to your AI Analyzer!
        // Example: commandGateway.send(new AnalyzeSymptomWithAiCommand(event.id(), diagnosticReport));
    }
}


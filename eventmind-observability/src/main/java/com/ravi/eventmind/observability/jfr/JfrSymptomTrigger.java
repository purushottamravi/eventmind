package com.ravi.eventmind.observability.jfr;

import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.axonframework.commandhandling.gateway.CommandGateway;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JfrSymptomTrigger {

    private final JfrAnalyzer jfrAnalyzer;
    private final CommandGateway commandGateway; // Inject Axon's command gateway!

    public JfrSymptomTrigger(JfrAnalyzer jfrAnalyzer, CommandGateway commandGateway) {
        this.jfrAnalyzer = jfrAnalyzer;
        this.commandGateway = commandGateway;
    }

    @EventHandler
    public void on(SymptomCreatedEvent event) {
        log.info("Symptom detected: {}. Extracting current JFR snapshot.", event.name());

        // 1. Who calls whom: Trigger calls Analyzer, Analyzer reads JfrReport and clears it
        String diagnosticReportText = jfrAnalyzer.analyze();

        log.info("JFR Analysis Report compiled successfully for symptom ID: {}", event.id());

        // 2. The Final Chain Link: Dispatch the text report to your AI Command handler!
        commandGateway.send(new AiAnalysisRequest(
                event.id(),
                event.name(),
                diagnosticReportText
        ));

        log.info("Dispatched AnalyzeSymptomWithAiCommand to the AI routing module.");
    }
}


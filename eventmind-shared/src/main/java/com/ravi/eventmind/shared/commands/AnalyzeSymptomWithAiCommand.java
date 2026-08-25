package com.ravi.eventmind.shared.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
public record AnalyzeSymptomWithAiCommand(
        @TargetAggregateIdentifier
        String symptomId,
        String symptomName,
        String diagnosticReportText
) {}

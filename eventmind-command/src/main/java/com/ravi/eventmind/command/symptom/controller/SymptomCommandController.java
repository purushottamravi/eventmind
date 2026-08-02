package com.ravi.eventmind.command.symptom.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.command.symptom.commands.CreateSymptomCommand;
import com.ravi.eventmind.shared.annotations.AuditLog;
import com.ravi.eventmind.shared.constants.enums.OriginType;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import com.ravi.eventmind.shared.validation.SchemaValidation;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.AggregateStreamCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Takes HTTP calls to create symptoms, validates the body, and pushes a command into Axon. No business logic lives here.
 */
@RestController
@RequestMapping("/symptoms")
@Slf4j
public class SymptomCommandController {

    private final CommandGateway commandGateway;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;

    public SymptomCommandController(CommandGateway commandGateway,
                                    @Qualifier("symptomRequestSchemaValidator") JsonSchemaValidator jsonSchemaValidator,
                                    ObjectMapper objectMapper) {
        this.commandGateway = commandGateway;
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @AuditLog("Create symptom operation")
    public String addSymptoms(@RequestBody JsonNode body) {
        SchemaValidation.requireValid(jsonSchemaValidator, body);

        SymptomRestModel symptomRestModel;
        try {
            symptomRestModel = objectMapper.treeToValue(body, SymptomRestModel.class);
        } catch (JsonProcessingException e) {
            throw new EventMindExceptions(
                    EventMindReasonsEnum.INVALID_REQUEST_BODY,
                    "Request body could not be mapped: " + e.getOriginalMessage(),
                    e
            );
        }

        String id = resolveId(symptomRestModel);
        Long origin = OriginType.cumulateAll(
                symptomRestModel.origin().stream()
                        .map(OriginType::fromName)
                        .map(OriginType::getId)
                        .toList());
        CreateSymptomCommand createSymptomCommand = new CreateSymptomCommand(
                id,
                symptomRestModel.name(),
                origin,
                symptomRestModel.numberOfOccurance(),
                CorrelationId.get());

        try {
            commandGateway.sendAndWait(createSymptomCommand);
        } catch (AggregateStreamCreationException e) {
            log.info("Symptom {} already exists; ignoring duplicate create (at-least-once delivery)", id);
        }

        return "Created Symptom with Id : " + id;
    }

    private String resolveId(SymptomRestModel model) {
        if (model.id() != null && !model.id().isBlank()) {
            return model.id();
        }
        return UUID.randomUUID().toString();
    }
}

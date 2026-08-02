package com.ravi.eventmind.ai.logging.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.ai.logging.IngestLogRequest;
import com.ravi.eventmind.ai.logging.LogStore;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import com.ravi.eventmind.shared.validation.SchemaValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The public door for pushing log entries into the AI module's log store. Producers
 * like the observability service call here so the AI module never pokes another
 * service's database.
 */
@RestController
@RequestMapping("/logs")
public class ApplicationLogController {

    private final LogStore logStore;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;

    public ApplicationLogController(LogStore logStore,
                                    @Qualifier("ingestLogRequestSchemaValidator") JsonSchemaValidator ingestLogRequestSchemaValidator,
                                    ObjectMapper objectMapper) {
        this.logStore = logStore;
        this.jsonSchemaValidator = ingestLogRequestSchemaValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public void ingest(@RequestBody JsonNode body,
                       @RequestHeader(value = CorrelationId.HEADER, required = false) String correlationId) {
        SchemaValidation.requireValid(jsonSchemaValidator, body);

        IngestLogRequest request;
        try {
            request = objectMapper.treeToValue(body, IngestLogRequest.class);
        } catch (JsonProcessingException e) {
            throw new EventMindExceptions(
                    EventMindReasonsEnum.INVALID_REQUEST_BODY,
                    "Request body could not be mapped: " + e.getOriginalMessage(),
                    e
            );
        }
        logStore.save(request.level(), request.message(), request.exception(), correlationId);
    }
}

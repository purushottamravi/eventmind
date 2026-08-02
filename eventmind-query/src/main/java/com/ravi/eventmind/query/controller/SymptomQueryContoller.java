package com.ravi.eventmind.query.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ravi.eventmind.query.queries.GetSymptomsQuery;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import com.ravi.eventmind.shared.validation.SchemaValidation;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * The front door for symptom queries. Takes HTTP calls, runs them past the schema
 * validator, asks the query gateway, and sends the answers back as JSON.
 */
@RestController
@RequestMapping("/symptoms")
@Slf4j
public class SymptomQueryContoller {
    private final QueryGateway queryGateway;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;

    public SymptomQueryContoller(QueryGateway queryGateway,
                                 @Qualifier("symptomQuerySchemaValidator") JsonSchemaValidator symptomQuerySchemaValidator,
                                 ObjectMapper objectMapper) {
        this.queryGateway = queryGateway;
        this.jsonSchemaValidator = symptomQuerySchemaValidator;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<SymptomRestModel> getAllSymptoms(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ObjectNode params = objectMapper.createObjectNode();
        if (name != null) {
            params.put("name", name);
        }
        params.put("page", page);
        params.put("size", size);
        SchemaValidation.requireValid(jsonSchemaValidator, params);

        GetSymptomsQuery getSymptomsQuery = new GetSymptomsQuery(name, page, size);

        List<SymptomRestModel> productRestModels = queryGateway.query(getSymptomsQuery, ResponseTypes.multipleInstancesOf(SymptomRestModel.class)).join();

        return productRestModels;
    }
}

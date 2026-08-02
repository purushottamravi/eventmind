package com.ravi.eventmind.command.healing.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.command.healing.dto.HealingPlan;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import com.ravi.eventmind.shared.validation.SchemaValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healing")
public class HealingController {

    private final HealingService healingService;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;

    HealingController(HealingService healingService,
                      @Qualifier("healingPlanSchemaValidator") JsonSchemaValidator healingPlanSchemaValidator,
                      ObjectMapper objectMapper) {
        this.healingService = healingService;
        this.jsonSchemaValidator = healingPlanSchemaValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/approve")
    public void approve(@RequestBody JsonNode body) {
        SchemaValidation.requireValid(jsonSchemaValidator, body);

        HealingPlan plan;
        try {
            plan = objectMapper.treeToValue(body, HealingPlan.class);
        } catch (JsonProcessingException e) {
            throw new EventMindExceptions(
                    EventMindReasonsEnum.INVALID_REQUEST_BODY,
                    "Request body could not be mapped: " + e.getOriginalMessage(),
                    e
            );
        }
        healingService.execute(plan);
    }
}

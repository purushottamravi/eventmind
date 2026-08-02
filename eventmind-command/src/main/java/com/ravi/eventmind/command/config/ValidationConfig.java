package com.ravi.eventmind.command.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wires up the JSON schema validators used to check incoming requests. Spring reads these beans so controllers can just ask for one by name.
 */
@Configuration
public class ValidationConfig {

    @Bean
    public JsonSchemaValidator symptomRequestSchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/symptom-command-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }

    @Bean
    public JsonSchemaValidator healingPlanSchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/healing-plan-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }
}

package com.ravi.eventmind.query.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Just the wiring for the JSON schema validator. Loads the symptom-query schema file
 * and hands back a ready-to-use validator so the controller can check requests with it.
 */
@Configuration
public class ValidationConfig {

    @Bean
    public JsonSchemaValidator symptomQuerySchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/symptom-query-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }
}

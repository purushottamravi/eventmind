package com.ravi.eventmind.query.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ValidationConfig {

    @Bean
    public JsonSchemaValidator symptomQuerySchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/symptom-query-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }
}

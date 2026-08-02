package com.ravi.eventmind.ai.config;

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
    public JsonSchemaValidator analyzeRequestSchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/analyze-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }

    @Bean
    public JsonSchemaValidator approvalRequestSchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/approval-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }

    @Bean
    public JsonSchemaValidator ingestLogRequestSchemaValidator(ObjectMapper objectMapper) throws IOException {
        try (InputStream in = new ClassPathResource("schema/ingest-log-request.schema.json").getInputStream()) {
            return JsonSchemaValidator.from(in, objectMapper);
        }
    }
}

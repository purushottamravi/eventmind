package com.ravi.eventmind.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.queryhandling.QueryMessage;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * Single shared Axon configuration, previously duplicated in both the command and
 * query modules. Registered as a Spring Boot auto-configuration so every Axon-based
 * module picks it up automatically.
 *
 * <p>Besides the shared serializers it registers dispatch interceptors that attach
 * the current thread's {@link CorrelationId} (seeded from the REST filter) to every
 * dispatched command and query. The command/event metadata flows with the message
 * across processes, so downstream event handlers can read it via
 * {@code @MetaDataValue("correlationId")}.</p>
 */
@AutoConfiguration
@ConditionalOnClass({Serializer.class, JacksonSerializer.class})
public class AxonConfig {

    @Bean
    @Primary
    public Serializer generalSerializer(ObjectMapper objectMapper) {
        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public Serializer eventSerializer(ObjectMapper objectMapper) {
        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

    @Bean
    public MessageDispatchInterceptor<CommandMessage<?>> correlationIdCommandDispatchInterceptor() {
        return messages -> (index, message) -> {
            String correlationId = CorrelationId.get();
            if (correlationId == null || correlationId.isBlank()) {
                return message;
            }
            return message.andMetaData(Map.of(CorrelationId.METADATA_KEY, correlationId));
        };
    }

    @Bean
    public MessageDispatchInterceptor<QueryMessage<?, ?>> correlationIdQueryDispatchInterceptor() {
        return messages -> (index, message) -> {
            String correlationId = CorrelationId.get();
            if (correlationId == null || correlationId.isBlank()) {
                return message;
            }
            return message.andMetaData(Map.of(CorrelationId.METADATA_KEY, correlationId));
        };
    }
}

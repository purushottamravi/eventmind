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
 * The one shared Axon config every EventMind module relies on, registered as a
 * Spring Boot auto-configuration so nobody has to wire it up by hand. On top of
 * the shared serializers it attaches the current thread's correlation ID to every
 * dispatched command and query, so downstream handlers can read it via
 * {@code @MetaDataValue("correlationId")}.
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

package com.ravi.eventmind.observability.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.extensions.kafka.eventhandling.consumer.streamable.StreamableKafkaMessageSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Points the observability event handler at the Kafka topic instead of Axon Server's
 * event stream. Without this the {@code observability} tracking processor would keep
 * polling the event store as its message source; explicitly registering it against the
 * auto-configured {@link StreamableKafkaMessageSource} is what makes Kafka the event
 * transport between the command side and the observability module.
 */
@Configuration
@ConditionalOnClass(StreamableKafkaMessageSource.class)
public class KafkaEventProcessingConfig {

    private static final String PROCESSING_GROUP = "observability";

    public KafkaEventProcessingConfig(EventProcessingConfigurer eventProcessingConfigurer,
                                      StreamableKafkaMessageSource<String, byte[]> streamableKafkaMessageSource) {
        eventProcessingConfigurer.registerTrackingEventProcessor(
                PROCESSING_GROUP,
                configuration -> streamableKafkaMessageSource
        );
    }
}

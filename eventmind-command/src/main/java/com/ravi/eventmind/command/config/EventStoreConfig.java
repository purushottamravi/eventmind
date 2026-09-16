package com.ravi.eventmind.command.config;

import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the embedded in-memory event store so event-sourced aggregates still
 * work when Axon Server is removed. Events are stored locally (lost on restart,
 * which is fine for this demo) and published onto the shared Kafka topic by the
 * KafkaEventPublisher extension. Axon's auto-configuration builds the
 * EmbeddedEventStore on top of this engine.
 */
@Configuration
public class EventStoreConfig {

    @Bean
    public EventStorageEngine eventStorageEngine() {
        return new InMemoryEventStorageEngine();
    }
}
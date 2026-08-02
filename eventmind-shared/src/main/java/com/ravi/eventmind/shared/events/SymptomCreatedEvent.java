package com.ravi.eventmind.shared.events;

import org.axonframework.serialization.Revision;

/**
 * The news bulletin every module has agreed to understand: a symptom was created, here are the details.
 */
@Revision("1.0")
public record SymptomCreatedEvent(String id, String name, Long origin, Integer numberOfOccurance, String correlationId) {
}

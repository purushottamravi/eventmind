package com.ravi.eventmind.shared.events;

import org.axonframework.serialization.Revision;

@Revision("1.0")
public record SymptomCreatedEvent(String id, String name, Long origin, Integer numberOfOccurance, String correlationId) {
}

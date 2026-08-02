package com.ravi.eventmind.shared.dto;

import java.util.List;

/**
 * Shared REST representation of a symptom, used by both the command side
 * (request body for POST /symptoms) and the query side (read model for
 * GET /symptoms) so the API contract lives in one place.
 *
 * <p>{@code origin} carries the symbolic {@code OriginType} names (for example
 * {@code ["A","B"]}); the command side cumulates them into a numeric bitmask
 * and the query side decodes the stored bitmask back to the names.</p>
 */
public record SymptomRestModel(String id, String name, List<String> origin, Integer numberOfOccurance) {
}

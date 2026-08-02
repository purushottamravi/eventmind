package com.ravi.eventmind.shared.dto;

import java.util.List;

/**
 * The shared REST shape of a symptom, used on both sides: the command side takes
 * it as the POST body, the query side returns it from GETs, so the API contract
 * lives in one place. {@code origin} carries the symbolic OriginType names like
 * {@code ["A","B"]}; the command side turns those into a bitmask and the query
 * side decodes the bitmask back to names.
 */
public record SymptomRestModel(String id, String name, List<String> origin, Integer numberOfOccurance) {
}

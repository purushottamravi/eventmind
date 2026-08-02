package com.ravi.eventmind.shared.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;

import java.util.List;

/**
 * Shared entry point for enforcing the REST request contract defined by a JSON schema
 * resource. Violations surface as an {@link EventMindExceptions} with the shared
 * {@code INVALID_REQUEST_BODY} code so every module returns the same RFC-7807 body.
 */
public final class SchemaValidation {

    private SchemaValidation() {
    }

    public static void requireValid(JsonSchemaValidator validator, JsonNode body) {
        List<String> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            throw new EventMindExceptions(
                    EventMindReasonsEnum.INVALID_REQUEST_BODY,
                    String.join("; ", violations));
        }
    }
}

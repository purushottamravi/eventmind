package com.ravi.eventmind.shared.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A small, dependency-free JSON Schema (draft-07) validator that enforces the REST
 * request contract defined by a schema resource. It only knows the keywords
 * EventMind schemas actually use — type, required, properties, length/range/pattern,
 * enum, items, item/property counts, uniqueness — and cheerfully ignores the rest.
 */
public class JsonSchemaValidator {

    private final JsonNode schema;

    public JsonSchemaValidator(JsonNode schema) {
        this.schema = schema;
    }

    public static JsonSchemaValidator from(InputStream schemaJson, ObjectMapper mapper) throws IOException {
        return new JsonSchemaValidator(mapper.readTree(schemaJson));
    }

    public List<String> validate(JsonNode document) {
        List<String> errors = new ArrayList<>();
        validateNode(schema, document, "$", errors);
        return errors;
    }

    private void validateNode(JsonNode nodeSchema, JsonNode node, String path, List<String> errors) {
        if (nodeSchema == null || !nodeSchema.isObject()) {
            return;
        }

        validateEnum(nodeSchema, node, path, errors);

        List<String> allowed = allowedTypes(nodeSchema);
        if (allowed.isEmpty()) {
            return;
        }

        String matched = null;
        for (String type : allowed) {
            if (matchesType(node, type)) {
                matched = type;
                break;
            }
        }
        if (matched == null) {
            errors.add(path + ": must be of type " + String.join(" or ", allowed));
            return;
        }

        switch (matched) {
            case "object" -> validateObject(nodeSchema, node, path, errors);
            case "array" -> validateArray(nodeSchema, node, path, errors);
            case "string" -> validateString(nodeSchema, node, path, errors);
            case "integer", "number" -> validateNumber(nodeSchema, node, path, errors);
            default -> {
            }
        }
    }

    private List<String> allowedTypes(JsonNode nodeSchema) {
        JsonNode type = nodeSchema.get("type");
        List<String> allowed = new ArrayList<>();
        if (type == null || type.isNull()) {
            return allowed;
        }
        if (type.isTextual()) {
            allowed.add(type.asText());
        } else if (type.isArray()) {
            for (JsonNode t : type) {
                if (t.isTextual()) {
                    allowed.add(t.asText());
                }
            }
        }
        return allowed;
    }

    private boolean matchesType(JsonNode node, String type) {
        return switch (type) {
            case "null" -> node.isNull();
            case "boolean" -> node.isBoolean();
            case "object" -> node.isObject();
            case "array" -> node.isArray();
            case "number" -> node.isNumber();
            case "integer" -> node.isIntegralNumber();
            case "string" -> node.isTextual();
            default -> false;
        };
    }

    private void validateEnum(JsonNode nodeSchema, JsonNode node, String path, List<String> errors) {
        JsonNode enumNode = nodeSchema.get("enum");
        if (enumNode == null || !enumNode.isArray()) {
            return;
        }
        for (JsonNode allowed : enumNode) {
            if (allowed.equals(node)) {
                return;
            }
        }
        errors.add(path + ": must be one of " + enumNode);
    }

    private void validateObject(JsonNode nodeSchema, JsonNode node, String path, List<String> errors) {
        JsonNode required = nodeSchema.get("required");
        if (required != null && required.isArray()) {
            for (JsonNode field : required) {
                if (field.isTextual() && !node.hasNonNull(field.asText())) {
                    errors.add(path + ": " + field.asText() + " is required");
                }
            }
        }

        JsonNode properties = nodeSchema.get("properties");
        if (properties != null && properties.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (node.has(entry.getKey())) {
                    validateNode(entry.getValue(), node.get(entry.getKey()), path + "." + entry.getKey(), errors);
                }
            }
        }

        JsonNode additionalProperties = nodeSchema.get("additionalProperties");
        if (additionalProperties != null && additionalProperties.isBoolean() && !additionalProperties.asBoolean()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (properties == null || !properties.has(name)) {
                    errors.add(path + "." + name + ": additional property not allowed");
                }
            }
        }

        JsonNode minProperties = nodeSchema.get("minProperties");
        if (minProperties != null && minProperties.isIntegralNumber() && node.size() < minProperties.asInt()) {
            errors.add(path + ": must contain at least " + minProperties.asInt() + " properties");
        }
        JsonNode maxProperties = nodeSchema.get("maxProperties");
        if (maxProperties != null && maxProperties.isIntegralNumber() && node.size() > maxProperties.asInt()) {
            errors.add(path + ": must contain at most " + maxProperties.asInt() + " properties");
        }
    }

    private void validateArray(JsonNode nodeSchema, JsonNode node, String path, List<String> errors) {
        JsonNode items = nodeSchema.get("items");
        if (items != null && items.isObject()) {
            for (JsonNode item : node) {
                validateNode(items, item, path + "[]", errors);
            }
        }

        JsonNode minItems = nodeSchema.get("minItems");
        if (minItems != null && minItems.isIntegralNumber() && node.size() < minItems.asInt()) {
            errors.add(path + ": must contain at least " + minItems.asInt() + " items");
        }
        JsonNode maxItems = nodeSchema.get("maxItems");
        if (maxItems != null && maxItems.isIntegralNumber() && node.size() > maxItems.asInt()) {
            errors.add(path + ": must contain at most " + maxItems.asInt() + " items");
        }

        JsonNode uniqueItems = nodeSchema.get("uniqueItems");
        if (uniqueItems != null && uniqueItems.isBoolean() && uniqueItems.asBoolean()) {
            Set<JsonNode> seen = new HashSet<>();
            for (JsonNode item : node) {
                if (!seen.add(item)) {
                    errors.add(path + ": must not contain duplicate items");
                    break;
                }
            }
        }
    }

    private void validateString(JsonNode nodeSchema, JsonNode node, String path, List<String> errors) {
        String value = node.asText();
        int length = value.codePointCount(0, value.length());

        JsonNode minLength = nodeSchema.get("minLength");
        if (minLength != null && minLength.isIntegralNumber() && length < minLength.asInt()) {
            errors.add(path + ": must be at least " + minLength.asInt() + " characters");
        }

        JsonNode maxLength = nodeSchema.get("maxLength");
        if (maxLength != null && maxLength.isIntegralNumber() && length > maxLength.asInt()) {
            errors.add(path + ": must be at most " + maxLength.asInt() + " characters");
        }
        JsonNode pattern = nodeSchema.get("pattern");
        if (pattern != null && pattern.isTextual() && !value.matches(pattern.asText())) {
            errors.add(path + ": must match pattern " + pattern.asText());
        }
    }

    private void validateNumber(JsonNode nodeSchema, JsonNode node, String path, List<String> errors) {
        BigDecimal value = node.decimalValue();

        JsonNode minimum = nodeSchema.get("minimum");
        if (minimum != null && minimum.isNumber() && value.compareTo(minimum.decimalValue()) < 0) {
            errors.add(path + ": must be greater than or equal to " + minimum);
        }
        JsonNode maximum = nodeSchema.get("maximum");
        if (maximum != null && maximum.isNumber() && value.compareTo(maximum.decimalValue()) > 0) {
            errors.add(path + ": must be less than or equal to " + maximum);
        }
        JsonNode exclusiveMinimum = nodeSchema.get("exclusiveMinimum");
        if (exclusiveMinimum != null && exclusiveMinimum.isNumber()
                && value.compareTo(exclusiveMinimum.decimalValue()) <= 0) {
            errors.add(path + ": must be greater than " + exclusiveMinimum);
        }
        JsonNode exclusiveMaximum = nodeSchema.get("exclusiveMaximum");
        if (exclusiveMaximum != null && exclusiveMaximum.isNumber()
                && value.compareTo(exclusiveMaximum.decimalValue()) >= 0) {
            errors.add(path + ": must be less than " + exclusiveMaximum);
        }
        JsonNode multipleOf = nodeSchema.get("multipleOf");
        if (multipleOf != null && multipleOf.isNumber()
                && multipleOf.decimalValue().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal divisor = multipleOf.decimalValue();
            if (value.remainder(divisor).compareTo(BigDecimal.ZERO) != 0) {
                errors.add(path + ": must be a multiple of " + multipleOf);
            }
        }
    }
}

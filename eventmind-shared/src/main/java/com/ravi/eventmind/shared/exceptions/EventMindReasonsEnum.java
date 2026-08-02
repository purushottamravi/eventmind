package com.ravi.eventmind.shared.exceptions;

public enum EventMindReasonsEnum implements IReason {

    ID_IS_MANDATORY("symptom.can.found.by.id", "C001"),

    RECORD_NOT_FOUND("record.not.found", "C002"),

    INVALID_REQUEST_BODY("invalid.request.body", "C003"),

    SYMPTOM_INVALID("invalid.symptom", "C004"),

    RECOMMENDATION_NOT_FOUND("recommendation.not.found", "C005"),

    RECOMMENDATION_STATE_CONFLICT("recommendation.state.conflict", "C006"),

    EXECUTION_FAILED("execution.failed", "C007"),

    INTERNAL_ERROR("internal.error", "C008"),
    ;
    private String key;
    private String errorCode;

    EventMindReasonsEnum(final String key, final String errorCode) {
        this.key = key;
        this.errorCode = errorCode;
    }

    @Override
    public String getKey() {
        return key;
    }


    @Override
    public Integer getMaximumCharacter() {
        return 0;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }



    public static EventMindReasonsEnum getReason(String key) {
        for (EventMindReasonsEnum r : values()) {
            final String key2 = r.getKey();
            if (key2.equals(key)) {
                return r;
            }
        }
        return null;
    }

    public static EventMindReasonsEnum getByName(String key) {
        for (EventMindReasonsEnum r : values()) {
            final String key2 = r.name();
            if (key2.equals(key)) {
                return r;
            }
        }
        return null;
    }
}

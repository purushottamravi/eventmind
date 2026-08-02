package com.ravi.eventmind.shared.exceptions;

/**
 * The one exception to throw for domain/API errors, and it's framework-agnostic
 * so any module can use it without pulling in Axon. If Axon wraps it in a
 * {@code CommandExecutionException}, the shared advice unwraps the cause so every
 * module still answers with the same error shape.
 */
public class EventMindExceptions extends RuntimeException {

    private final IReason reason;

    public EventMindExceptions(IReason reason) {
        this(reason, reason != null ? reason.getKey() : null);
    }

    public EventMindExceptions(IReason reason, String message) {
        super(message, null);
        this.reason = reason;
    }

    public EventMindExceptions(IReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public IReason getReason() {
        return reason;
    }

    public String getErrorCode() {
        return reason != null ? reason.getErrorCode() : null;
    }
}

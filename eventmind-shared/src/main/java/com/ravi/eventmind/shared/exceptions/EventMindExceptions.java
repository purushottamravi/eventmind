package com.ravi.eventmind.shared.exceptions;

/**
 * Domain/API exception used across EventMind. Framework-agnostic so any module
 * (command, query, AI, observability) can throw it without depending on Axon.
 *
 * <p>When thrown from an Axon command handler or aggregate, the command gateway wraps
 * it in a {@code CommandExecutionException}; the shared RFC-7807 advice unwraps the
 * cause so every module produces the same error contract.
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

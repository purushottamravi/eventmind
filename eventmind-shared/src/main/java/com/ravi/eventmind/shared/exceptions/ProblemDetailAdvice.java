package com.ravi.eventmind.shared.exceptions;

import org.axonframework.commandhandling.CommandExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;

/**
 * The one place that turns exceptions into RFC-7807 problem+json responses for
 * every EventMind module. Every error body comes out the same way: the standard
 * type/title/status/detail fields plus an EventMind errorCode and message key as
 * custom properties.
 */
@RestControllerAdvice
public class ProblemDetailAdvice {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailAdvice.class);

    private static final String PROBLEMS_BASE = "https://eventmind.ravi.dev/problems/";

    private static final String GENERIC_BAD_REQUEST_DETAIL = "Request could not be processed";
    private static final String GENERIC_INTERNAL_ERROR_DETAIL = "An unexpected internal error occurred";

    @ExceptionHandler(EventMindExceptions.class)
    public ProblemDetail handleEventMindExceptions(EventMindExceptions ex) {
        return toProblemDetail(ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Request rejected with illegal argument", ex);
        return problemDetail(
                HttpStatus.BAD_REQUEST,
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getErrorCode(),
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getKey(),
                GENERIC_BAD_REQUEST_DETAIL);
    }

    @ExceptionHandler(CommandExecutionException.class)
    public ProblemDetail handleCommandExecution(CommandExecutionException ex) {
        if (ex.getCause() instanceof EventMindExceptions eventMindException) {
            return toProblemDetail(eventMindException);
        }
        log.error("Command execution failed", ex);
        return problemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                EventMindReasonsEnum.INTERNAL_ERROR.getErrorCode(),
                EventMindReasonsEnum.INTERNAL_ERROR.getKey(),
                GENERIC_INTERNAL_ERROR_DETAIL);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problemDetail(
                HttpStatus.BAD_REQUEST,
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getErrorCode(),
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getKey(),
                "Request body is missing or malformed");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problemDetail(
                HttpStatus.BAD_REQUEST,
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getErrorCode(),
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getKey(),
                "Parameter " + ex.getName() + " must be of type " + ex.getRequiredType().getSimpleName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return problemDetail(
                HttpStatus.BAD_REQUEST,
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getErrorCode(),
                EventMindReasonsEnum.INVALID_REQUEST_BODY.getKey(),
                "Request body failed validation: " + ex.getBody().getDetail());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error while handling request", ex);
        return problemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                EventMindReasonsEnum.INTERNAL_ERROR.getErrorCode(),
                EventMindReasonsEnum.INTERNAL_ERROR.getKey(),
                GENERIC_INTERNAL_ERROR_DETAIL);
    }

    private ProblemDetail toProblemDetail(EventMindExceptions ex) {
        EventMindReasonsEnum reason = ex.getReason() instanceof EventMindReasonsEnum r ? r : null;
        HttpStatus status = reason != null ? resolveStatus(reason) : HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : EventMindReasonsEnum.INTERNAL_ERROR.getErrorCode();
        String key = reason != null ? reason.getKey() : EventMindReasonsEnum.INTERNAL_ERROR.getKey();
        if (ex.getCause() != null) {
            log.error("Domain error {} propagated with underlying cause: {}", key, ex.getMessage(), ex.getCause());
        }
        return problemDetail(status, errorCode, key, ex.getMessage());
    }

    private ProblemDetail problemDetail(HttpStatusCode status, String errorCode, String key, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEMS_BASE + errorCode));
        problem.setTitle(key);
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("key", key);
        return problem;
    }

    private HttpStatus resolveStatus(EventMindReasonsEnum reason) {
        return switch (reason) {
            case RECORD_NOT_FOUND, RECOMMENDATION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ID_IS_MANDATORY, INVALID_REQUEST_BODY, SYMPTOM_INVALID -> HttpStatus.BAD_REQUEST;
            case RECOMMENDATION_STATE_CONFLICT -> HttpStatus.CONFLICT;
            case EXECUTION_FAILED, INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

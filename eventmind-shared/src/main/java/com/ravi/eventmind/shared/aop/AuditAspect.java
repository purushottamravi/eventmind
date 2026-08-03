package com.ravi.eventmind.shared.aop;

import com.ravi.eventmind.shared.aop.event.MethodExecutionEvent;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches every method tagged with {@code @AuditLog} and records how it went -
 * which class, which method, how long it took and whether it blew up. The
 * outcome goes through the normal logging pipeline, is handed to the optional
 * {@link AuditLogRecorder} for persistence, and a JFR event is committed when
 * recording is active. Lives in shared so it fires in every module, not just the
 * one that happened to declare it.
 */
@Aspect
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRecorder recorder;

    public AuditAspect(AuditLogRecorder recorder) {
        this.recorder = recorder;
    }

    @Around("@annotation(com.ravi.eventmind.shared.annotations.AuditLog)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        MethodExecutionEvent jfrEvent = new MethodExecutionEvent();
        jfrEvent.className = className;
        jfrEvent.methodName = methodName;
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            jfrEvent.executionTime = duration;
            jfrEvent.status = "SUCCESS";
            String message = String.format("Completed %s.%s successfully in %d ms", className, methodName, duration);
            log.info(message);
            record("INFO", message, null);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            jfrEvent.executionTime = duration;
            jfrEvent.status = "FAILED";
            jfrEvent.exception = ex.getClass().getName();
            String message = String.format("Failed %s.%s after %d ms", className, methodName, duration);
            log.error(message, ex);
            record("ERROR", message, ex);
            throw ex;
        } finally {
            if (jfrEvent.isEnabled()) {
                jfrEvent.commit();
            }
        }
    }

    private void record(String level, String message, Exception exception) {
        try {
            recorder.record(level, message, exception, CorrelationId.get());
        } catch (RuntimeException ex) {
            log.warn("Audit recorder could not store the log entry; skipping: {}", ex.getMessage());
        }
    }
}

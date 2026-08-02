package com.ravi.eventmind.observability.aop;


import com.ravi.eventmind.observability.jfr.event.MethodExecutionEvent;
import com.ravi.eventmind.observability.logging.ApplicationLogService;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Logs every execution of a method tagged with {@code @AuditLog} - which class,
 * which method, how long it took and whether it succeeded. The details land in
 * the application log and optionally go out as a JFR event.
 */


@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final ApplicationLogService applicationLogService;

    public AuditAspect(ApplicationLogService applicationLogService) {
        this.applicationLogService = applicationLogService;
    }


    /**
     * Intercepts annotated methods and records their execution details.
     *
     * <p>The method measures execution time, logs successful and failed
     * executions, stores application audit information, and commits a
     * Java Flight Recorder event when recording is enabled.
     *
     * @param joinPoint provides information about the intercepted method,
     *                  including class and method details
     *
     * @return the original method execution result
     *
     * @throws Throwable if the intercepted method throws an exception
     */

    @Around("@annotation(com.ravi.eventmind.shared.annotations.AuditLog)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        MethodExecutionEvent jfrEvent = new MethodExecutionEvent();
        jfrEvent.className = className;
        jfrEvent.methodName = methodName;
        try {
            log.info("Entering {}.{}", className, methodName);
            Object result = getLogObject(joinPoint, start, jfrEvent, className, methodName);
            return result;
        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - start;
            jfrEvent.executionTime = duration;
            jfrEvent.status = "FAILED";
            jfrEvent.exception = ex.getClass().getName();
            String message = String.format("Failed %s.%s after %d ms", className, methodName, duration);
            log.error(message, ex);
            applicationLogService.save("ERROR", message, ex, CorrelationId.get());
            throw ex;
        } finally {
            if (jfrEvent.isEnabled()) {
                jfrEvent.commit();
            }
        }
    }

    private Object getLogObject(ProceedingJoinPoint joinPoint, long start, MethodExecutionEvent jfrEvent, String className, String methodName) throws Throwable {
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        jfrEvent.executionTime = duration;
        jfrEvent.status = "SUCCESS";

        String message = String.format("Completed %s.%s successfully in %d ms", className, methodName, duration);
        log.info(message);
        applicationLogService.save("INFO", message, null, CorrelationId.get());
        return result;
    }
}
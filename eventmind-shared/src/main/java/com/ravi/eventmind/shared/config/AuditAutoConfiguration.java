package com.ravi.eventmind.shared.config;

import com.ravi.eventmind.shared.annotations.AuditLog;
import com.ravi.eventmind.shared.aop.AuditAspect;
import com.ravi.eventmind.shared.aop.AuditLogRecorder;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Stands up the {@link AuditAspect} whenever AOP is on the classpath so
 * {@code @AuditLog} methods get watched in every module, and drops in a no-op
 * {@link AuditLogRecorder} unless a module brings its own (observability does).
 */
@AutoConfiguration
@ConditionalOnClass({Aspect.class, AuditLog.class})
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditLogRecorder.class)
    public AuditLogRecorder defaultAuditLogRecorder() {
        return (level, message, exception, correlationId) -> {
        };
    }

    @Bean
    @ConditionalOnMissingBean(AuditAspect.class)
    public AuditAspect auditAspect(AuditLogRecorder auditLogRecorder) {
        return new AuditAspect(auditLogRecorder);
    }
}

package com.ravi.eventmind.shared.config;

import com.ravi.eventmind.shared.correlation.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registers the shared {@link CorrelationIdFilter} for every EventMind module that
 * exposes a servlet web application. Registered via {@code AutoConfiguration.imports}.
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class CorrelationIdAutoConfiguration {

    @Bean
    public OncePerRequestFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }
}

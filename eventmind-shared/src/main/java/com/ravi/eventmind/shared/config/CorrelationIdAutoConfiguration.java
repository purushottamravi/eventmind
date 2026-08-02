package com.ravi.eventmind.shared.config;

import com.ravi.eventmind.shared.correlation.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registers the shared correlation ID filter for every EventMind module that runs
 * a servlet web app, so requests get stamped without any per-module setup.
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class CorrelationIdAutoConfiguration {

    @Bean
    public OncePerRequestFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }
}

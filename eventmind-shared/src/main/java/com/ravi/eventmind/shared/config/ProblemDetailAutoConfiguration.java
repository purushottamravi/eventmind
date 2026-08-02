package com.ravi.eventmind.shared.config;

import com.ravi.eventmind.shared.exceptions.ProblemDetailAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ProblemDetail;

/**
 * Registers the shared RFC-7807 problem detail advice for every EventMind module,
 * but only kicks in when a web app with Spring HTTP support is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(ProblemDetail.class)
@ConditionalOnWebApplication
public class ProblemDetailAutoConfiguration {

    @Bean
    public ProblemDetailAdvice problemDetailAdvice() {
        return new ProblemDetailAdvice();
    }
}

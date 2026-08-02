package com.ravi.eventmind.observability.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async support for best-effort cross-service pushes (e.g. forwarding log
 * entries to the AI module) so they never block the Axon event processor.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "logIngestionExecutor")
    public Executor logIngestionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("log-ingestion-");
        executor.initialize();
        return executor;
    }
}

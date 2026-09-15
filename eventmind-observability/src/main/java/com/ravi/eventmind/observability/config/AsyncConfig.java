package com.ravi.eventmind.observability.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * The async executor configs for best-effort cross-service pushes (shipping log
 * entries and JFR diagnostic snapshots to the AI module) so they never block
 * the Axon event processor.
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

    @Bean(name = "aiAnalysisExecutor")
    public Executor aiAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ai-analysis-");
        executor.initialize();
        return executor;
    }
}

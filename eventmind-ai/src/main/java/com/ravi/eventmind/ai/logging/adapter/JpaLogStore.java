package com.ravi.eventmind.ai.logging.adapter;

import com.ravi.eventmind.ai.logging.entity.ApplicationLog;
import com.ravi.eventmind.ai.logging.port.LogStore;
import com.ravi.eventmind.ai.logging.repository.ApplicationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * The default {@link LogStore}, saving entries into the AI module's own log table.
 */
@Service
public class JpaLogStore implements LogStore {

    private final ApplicationLogRepository repository;

    public JpaLogStore(ApplicationLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(String level, String message, String exception, String correlationId) {
        ApplicationLog entity = new ApplicationLog();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLevel(level);
        entity.setMessage(message);
        entity.setException(exception);
        entity.setCorrelationId(correlationId);
        repository.save(entity);
    }
}

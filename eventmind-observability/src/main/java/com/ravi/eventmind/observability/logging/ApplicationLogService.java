package com.ravi.eventmind.observability.logging;

import com.ravi.eventmind.observability.logging.repository.ApplicationLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Hands {@link ApplicationLog} rows to the repository for saving - level, message,
 * optional exception and the correlation ID of the originating request.
 */
@Service
public class ApplicationLogService {
    private final ApplicationLogRepository repository;

    public ApplicationLogService(ApplicationLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves a application execution log.
     *
     * @param level log severity level
     * @param message description of the successful operation
     * @param exception optional exception information
     * @param correlationId correlation ID of the originating REST request (may be null)
     */
    @Transactional
    public void save( String level,  String message,Exception exception, String correlationId) {
        ApplicationLog log = new ApplicationLog();
        log.setCreatedAt(LocalDateTime.now());
        log.setLevel(level);
        log.setMessage(message);
        log.setCorrelationId(correlationId);
        if(exception != null) {
            log.setException(exception.getMessage());
        }
        repository.save(log);
    }

}

package com.ravi.eventmind.ai.logging;

import com.ravi.eventmind.ai.logging.repository.ApplicationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The default {@link LogProvider}, reading straight from the AI module's own log
 * table. If the store is unavailable we swallow the error and hand back an empty
 * context so the analysis and RAG pipeline just chug along.
 */
@Slf4j
@Service
public class JpaLogProvider implements LogProvider {

    private final ApplicationLogRepository repository;

    public JpaLogProvider(ApplicationLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LogEntry> recentLogs(int limit) {
        try {
            return repository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                    .getContent()
                    .stream()
                    .map(log -> new LogEntry(log.getId(), log.getLevel(), log.getMessage(), log.getException()))
                    .toList();
        } catch (Exception e) {
            log.warn("Could not read application logs; returning empty context", e);
            return List.of();
        }
    }
}

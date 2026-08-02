package com.ravi.eventmind.ai.rag.loader;

import com.ravi.eventmind.ai.knowledge.entity.DocumentEntity;
import com.ravi.eventmind.ai.knowledge.repository.DocumentRepository;
import com.ravi.eventmind.ai.logging.LogEntry;
import com.ravi.eventmind.ai.logging.LogProvider;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class DocumentLoader implements IDocumentLoader {

    private static final int LOG_FETCH_LIMIT = 200;

    private final DocumentRepository repository;
    private final LogProvider logProvider;

    public DocumentLoader(DocumentRepository repository, LogProvider logProvider) {
        this.repository = repository;
        this.logProvider = logProvider;
    }

    @Override
    public List<Document> load() {
        List<Document> documents = new ArrayList<>();
        repository.findAll().stream().map(this::toDocument).forEach(documents::add);
        logProvider.recentLogs(LOG_FETCH_LIMIT).stream().map(this::logToDocument).forEach(documents::add);
        return documents;
    }

    private Document toDocument(DocumentEntity entity) {

        return new Document(
                entity.getContent(),
                Map.of(
                        "Id", entity.getId(),
                        "source", entity.getSource(),
                        "type", entity.getType()
                )
        );
    }

    private Document logToDocument(LogEntry log) {
        StringBuilder content = new StringBuilder()
                .append('[').append(log.level()).append("] ")
                .append(log.message());
        if (log.exception() != null && !log.exception().isBlank()) {
            content.append("\nException: ").append(log.exception());
        }

        return new Document(
                content.toString(),
                Map.of(
                        "Id", String.valueOf(log.id()),
                        "source", "application-log",
                        "type", log.level()
                )
        );
    }
}

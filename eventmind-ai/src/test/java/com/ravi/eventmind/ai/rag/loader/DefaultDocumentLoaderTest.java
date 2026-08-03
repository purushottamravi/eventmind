package com.ravi.eventmind.ai.rag.loader;

import com.ravi.eventmind.ai.logging.model.LogEntry;
import com.ravi.eventmind.ai.logging.port.LogProvider;
import com.ravi.eventmind.ai.rag.entity.KnowledgeDocument;
import com.ravi.eventmind.ai.rag.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Proves the loader combines knowledge documents and recent logs into one list.
 */
@ExtendWith(MockitoExtension.class)
class DefaultDocumentLoaderTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private LogProvider logProvider;

    @Test
    void load_shouldCombineDocumentsAndLogs() {
        KnowledgeDocument entity = new KnowledgeDocument();
        entity.setId(1L);
        entity.setSource("runbook");
        entity.setType("guide");
        entity.setContent("Heap exhaustion causes OOM");

        LogEntry log = new LogEntry(7L, "ERROR", "Database timeout detected", "connect timed out");

        when(documentRepository.findAll()).thenReturn(List.of(entity));
        when(logProvider.recentLogs(200)).thenReturn(List.of(log));

        List<Document> result = new DefaultDocumentLoader(documentRepository, logProvider).load();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getText().contains("Heap exhaustion causes OOM")));
        assertTrue(result.stream().anyMatch(d -> d.getText().contains("[ERROR] Database timeout detected")));
    }

    @Test
    void load_shouldReturnDocumentsWhenNoLogs() {
        KnowledgeDocument entity = new KnowledgeDocument();
        entity.setId(1L);
        entity.setSource("runbook");
        entity.setType("guide");
        entity.setContent("Heap exhaustion causes OOM");

        when(documentRepository.findAll()).thenReturn(List.of(entity));
        when(logProvider.recentLogs(200)).thenReturn(List.of());

        List<Document> result = new DefaultDocumentLoader(documentRepository, logProvider).load();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getText().contains("Heap exhaustion causes OOM"));
    }
}

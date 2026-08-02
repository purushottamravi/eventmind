package com.ravi.eventmind.ai.rag.loader;

import com.ravi.eventmind.ai.knowledge.entity.DocumentEntity;
import com.ravi.eventmind.ai.knowledge.repository.DocumentRepository;
import com.ravi.eventmind.ai.logging.LogEntry;
import com.ravi.eventmind.ai.logging.LogProvider;
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
class DocumentLoaderTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private LogProvider logProvider;

    @Test
    void load_shouldCombineDocumentsAndLogs() {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(1L);
        entity.setSource("runbook");
        entity.setType("guide");
        entity.setContent("Heap exhaustion causes OOM");

        LogEntry log = new LogEntry(7L, "ERROR", "Database timeout detected", "connect timed out");

        when(documentRepository.findAll()).thenReturn(List.of(entity));
        when(logProvider.recentLogs(200)).thenReturn(List.of(log));

        List<Document> result = new DocumentLoader(documentRepository, logProvider).load();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getText().contains("Heap exhaustion causes OOM")));
        assertTrue(result.stream().anyMatch(d -> d.getText().contains("[ERROR] Database timeout detected")));
    }

    @Test
    void load_shouldReturnDocumentsWhenNoLogs() {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(1L);
        entity.setSource("runbook");
        entity.setType("guide");
        entity.setContent("Heap exhaustion causes OOM");

        when(documentRepository.findAll()).thenReturn(List.of(entity));
        when(logProvider.recentLogs(200)).thenReturn(List.of());

        List<Document> result = new DocumentLoader(documentRepository, logProvider).load();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getText().contains("Heap exhaustion causes OOM"));
    }
}

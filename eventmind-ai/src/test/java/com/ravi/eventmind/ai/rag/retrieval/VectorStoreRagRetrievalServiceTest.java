package com.ravi.eventmind.ai.rag.retrieval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Makes sure the vector search returns documents and falls back to empty on failure.
 */
@ExtendWith(MockitoExtension.class)
class VectorStoreRagRetrievalServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Test
    void search_shouldReturnRelevantDocuments() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("High memory pressure causes headaches")));

        VectorStoreRagRetrievalService service = new VectorStoreRagRetrievalService(vectorStore);

        List<Document> result = service.search("headache", 3);

        assertEquals(1, result.size());
        assertEquals("High memory pressure causes headaches", result.get(0).getText());
    }

    @Test
    void search_shouldReturnEmptyWhenVectorStoreFails() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("vector store unavailable"));

        VectorStoreRagRetrievalService service = new VectorStoreRagRetrievalService(vectorStore);

        assertTrue(service.search("headache", 3).isEmpty());
    }
}

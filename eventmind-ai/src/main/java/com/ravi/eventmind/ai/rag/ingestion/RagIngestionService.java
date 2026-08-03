package com.ravi.eventmind.ai.rag.ingestion;

import com.ravi.eventmind.ai.rag.chunker.DocumentChunker;
import com.ravi.eventmind.ai.rag.embedding.EmbeddingService;
import com.ravi.eventmind.ai.rag.loader.DocumentLoader;
import org.springframework.stereotype.Service;
/**
 * Glues the RAG pipeline together: load, chunk, embed, done.
 * The conductor for ingesting knowledge into the vector store.
 */
@Service
public class RagIngestionService {
    private final DocumentLoader loader;
    private final DocumentChunker chunker;
    private final EmbeddingService embeddingService;

    public RagIngestionService(DocumentLoader loader, DocumentChunker chunker, EmbeddingService embeddingService) {
        this.loader = loader;
        this.chunker = chunker;
        this.embeddingService = embeddingService;
    }


    public void ingest() {
        var documents = loader.load();
        var chunks =    chunker.chunk(documents);
        embeddingService.embed(chunks);
    }
}
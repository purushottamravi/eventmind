package com.ravi.eventmind.ai.rag.ingestion;

import com.ravi.eventmind.ai.rag.chunker.DocumentChunker;
import com.ravi.eventmind.ai.rag.embedding.EmbeddingService;
import com.ravi.eventmind.ai.rag.loader.IDocumentLoader;
import org.springframework.stereotype.Service;
@Service
public class RagIngestionService {
    private final IDocumentLoader loader;
    private final DocumentChunker chunker;
    private final EmbeddingService embeddingService;

    public RagIngestionService(IDocumentLoader loader, DocumentChunker chunker, EmbeddingService embeddingService) {
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
package com.ravi.eventmind.ai.rag.chunker;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Splits long documents into smaller pieces using a token-based splitter.
 * The "many small chunks" step so embeddings stay manageable.
 */
@Component
public class DefaultDocumentChunker implements DocumentChunker {
    private final TokenTextSplitter splitter;

    public DefaultDocumentChunker() {

        this.splitter = new TokenTextSplitter(
                500,   // chunk size
                100,   // minimum chunk size
                5,     // minimum chunk length
                10000, // max number of chunks
                true   // keep separators
        );
    }


    @Override
    public List<Document> chunk(List<Document> documents) {
        return splitter.apply(documents);
    }
}

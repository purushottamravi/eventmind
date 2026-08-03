package com.ravi.eventmind.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sanity check that the AI Spring context actually boots: all controllers,
 * services, validators, JPA repositories and shared auto-configurations wire up.
 *
 * <p>Postgres + pgvector are external, so the test runs on an in-memory H2
 * datasource with the pgvector auto-configuration excluded and a stub
 * {@link VectorStore} in its place. The Ollama model beans are created lazily
 * and never contacted, so no Ollama instance is required either. A true
 * Postgres/pgvector integration test (e.g. Testcontainers) can be layered on
 * top of this once the build environment has network access.
 */
@SpringBootTest
@ActiveProfiles("test")
class EventmindAiApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class InMemoryVectorStoreConfig {

        @Bean
        @Primary
        VectorStore inMemoryVectorStore() {
            List<Document> documents = new CopyOnWriteArrayList<>();
            return new VectorStore() {
                @Override
                public void add(List<Document> documents) {
                    documents.addAll(documents);
                }

                @Override
                public void delete(List<String> idList) {
                    documents.removeIf(d -> idList.contains(d.getId()));
                }

                @Override
                public void delete(Filter.Expression filterExpression) {
                    documents.clear();
                }

                @Override
                public List<Document> similaritySearch(SearchRequest request) {
                    return new ArrayList<>(documents);
                }
            };
        }
    }
}

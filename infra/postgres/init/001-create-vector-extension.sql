-- Enables the pgvector extension required by eventmind-ai's vector store.
-- Executed automatically when the Postgres container first initializes.
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. Подключаем расширения
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. Таблица document_info (основная)
CREATE TABLE document_info (
    id UUID PRIMARY KEY,
    file_name TEXT NOT NULL,
    extension TEXT NOT NULL,
    file_hash TEXT NOT NULL,
    chunk_count INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    summary TEXT,

    CONSTRAINT unique_file_hash UNIQUE (file_name, extension, file_hash)
);

-- 3. Таблица rag_chunks (векторы + FK)
CREATE TABLE rag_chunks (
    id           UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content      TEXT NOT NULL,
    embedding    VECTOR(768),
    type         VARCHAR(255),
    source       VARCHAR(255),
    file_name TEXT NOT NULL,
    extension TEXT NOT NULL,
    file_hash TEXT NOT NULL,
    chunk_index  INT,
    created_at   TIMESTAMP DEFAULT now(),

    CONSTRAINT fk_document FOREIGN KEY (file_name, extension, file_hash) REFERENCES document_info(file_name, extension, file_hash) ON DELETE CASCADE
);

CREATE INDEX ON rag_chunks USING ivfflat (embedding vector_cosine_ops);

-- hnsw index для embedding
CREATE INDEX spring_ai_rag_chunks_index
    ON rag_chunks
    USING hnsw (embedding vector_cosine_ops);



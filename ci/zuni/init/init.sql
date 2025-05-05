-- 1. Подключаем pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Подключаем uuid-ossp для генерации UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 3. Таблица хранения векторов
CREATE TABLE public.rag_chunks
(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    "content"   text NOT NULL,
    embedding   VECTOR(768) NULL,
    "type"      varchar(255) NULL,
    "source"    varchar(255) NULL,
    chunk_index int4 NULL,
    created_at  timestamp DEFAULT now() NULL,
    summary     text NULL
);

CREATE INDEX spring_ai_rag_chunks_index
    ON public.rag_chunks
    USING hnsw (embedding vector_cosine_ops);
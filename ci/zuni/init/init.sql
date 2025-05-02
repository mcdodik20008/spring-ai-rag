-- 1. Подключаем pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Подключаем uuid-ossp для генерации UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 3. Таблица хранения векторов
CREATE TABLE IF NOT EXISTS public.vector_store (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content TEXT,
    metadata JSON,
    embedding VECTOR(768)  -- указываем размерность!
    );

-- 4. Индекс для быстрого поиска ближайших векторов
CREATE INDEX IF NOT EXISTS spring_ai_vector_index
    ON public.vector_store
    USING hnsw (embedding vector_cosine_ops);


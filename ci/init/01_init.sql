-- 1. Расширения
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- 2. Основная таблица документов
CREATE TABLE IF NOT EXISTS document_info (
    id UUID PRIMARY KEY,
    file_name TEXT NOT NULL,
    extension TEXT NOT NULL,
    file_hash TEXT NOT NULL,
    chunk_count INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    summary TEXT,
    CONSTRAINT unique_file_hash UNIQUE (file_name, extension, file_hash)
);

-- 3. Чанки (вектора + метаданные)
CREATE TABLE IF NOT EXISTS rag_chunks (
    id           UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content      TEXT NOT NULL,
    embedding    VECTOR(768),
    type         VARCHAR(255),
    source       VARCHAR(255),
    file_name    TEXT NOT NULL,
    extension    TEXT NOT NULL,
    file_hash    TEXT NOT NULL,
    chunk_index  INT,
    created_at   TIMESTAMP DEFAULT now(),
    tsv          tsvector, -- теперь просто колонка под FTS
    CONSTRAINT fk_document FOREIGN KEY (file_name, extension, file_hash)
        REFERENCES document_info(file_name, extension, file_hash)
        ON DELETE CASCADE
);

-- 4. Индексы по вектору
CREATE INDEX IF NOT EXISTS rag_chunks_embedding_ivfflat
    ON rag_chunks USING ivfflat (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS rag_chunks_embedding_hnsw
    ON rag_chunks USING hnsw (embedding vector_cosine_ops);

-- 5. FTS конфиг с unaccent
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_ts_config WHERE cfgname = 'russian_unaccent') THEN
        CREATE TEXT SEARCH CONFIGURATION russian_unaccent (COPY = russian);
        ALTER TEXT SEARCH CONFIGURATION russian_unaccent
            ALTER MAPPING FOR hword, hword_part, word
            WITH unaccent, russian_stem;
    END IF;
END $$;

-- 6. Индекс для FTS
CREATE INDEX IF NOT EXISTS rag_chunks_tsv_gin_idx
    ON rag_chunks USING GIN (tsv);

-- 7. Триггер для автоматического обновления tsv
CREATE OR REPLACE FUNCTION rag_chunks_tsv_update() RETURNS trigger AS $$
BEGIN
    NEW.tsv := to_tsvector('russian_unaccent', unaccent(coalesce(NEW.content, '')));
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS rag_chunks_tsv_trg ON rag_chunks;
CREATE TRIGGER rag_chunks_tsv_trg
BEFORE INSERT OR UPDATE OF content ON rag_chunks
FOR EACH ROW EXECUTE FUNCTION rag_chunks_tsv_update();

-- 8. Таблица шаблонов промптов
CREATE TABLE IF NOT EXISTS chunking_prompt_templates (
    id UUID PRIMARY KEY,
    domain_name TEXT NOT NULL,
    user_description TEXT NOT NULL,
    generated_prompt TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    topic TEXT,
    topic_embedding VECTOR(768) -- подбери размер под свою модель
);

-- 9. Индексы для шаблонов
CREATE INDEX IF NOT EXISTS idx_cpt_topic_emb_ivfflat
    ON chunking_prompt_templates USING ivfflat (topic_embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_cpt_topic_trgm
    ON chunking_prompt_templates USING gin (topic gin_trgm_ops);

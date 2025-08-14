-- Таблица соответствий дубликатов
CREATE TABLE IF NOT EXISTS rag_chunk_duplicates (
    dup_id   UUID PRIMARY KEY REFERENCES rag_chunks(id) ON DELETE CASCADE,
    keep_id  UUID NOT NULL   REFERENCES rag_chunks(id) ON DELETE CASCADE,
    sim_score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_dup_not_self CHECK (dup_id <> keep_id)
);

-- Быстрый доступ по канону
CREATE INDEX IF NOT EXISTS idx_rag_chunk_dup_keep ON rag_chunk_duplicates(keep_id);

-- (опционально) VIEW для «канонического» id
CREATE OR REPLACE VIEW rag_chunks_canonical AS
SELECT
  rc.*,
  COALESCE(d.keep_id, rc.id) AS canonical_id
FROM rag_chunks rc
LEFT JOIN rag_chunk_duplicates d ON d.dup_id = rc.id;

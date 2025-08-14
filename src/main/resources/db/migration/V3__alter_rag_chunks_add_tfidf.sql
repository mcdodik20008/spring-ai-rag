ALTER TABLE rag_chunks
  ADD COLUMN IF NOT EXISTS tfidf JSONB,
  ADD COLUMN IF NOT EXISTS tfidf_norm DOUBLE PRECISION,
  ADD COLUMN IF NOT EXISTS tfidf_updated_at TIMESTAMP WITHOUT TIME ZONE;

-- индекс по ключам термов в JSONB
CREATE INDEX IF NOT EXISTS idx_rag_chunks_tfidf_gin ON rag_chunks USING GIN (tfidf jsonb_path_ops);

-- опционально: ускорить выборку «нуждается в перерасчёте»
CREATE INDEX IF NOT EXISTS idx_rag_chunks_tfidf_updated_at ON rag_chunks(tfidf_updated_at);

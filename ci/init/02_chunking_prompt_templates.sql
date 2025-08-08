CREATE TABLE chunking_prompt_templates (
    id UUID PRIMARY KEY,
    domain_name TEXT NOT NULL,
    user_description TEXT NOT NULL,
    generated_prompt TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Дополняем таблицу с шаблонами
ALTER TABLE chunking_prompt_templates
  ADD COLUMN IF NOT EXISTS topic TEXT,
  ADD COLUMN IF NOT EXISTS topic_embedding vector(768); -- подбери размер под свою модель

-- Индексы
-- ANN по вектору (ivfflat). Важно: сначала наполнить данными, затем индекс.
CREATE INDEX IF NOT EXISTS idx_cpt_topic_emb_ivfflat
  ON chunking_prompt_templates USING ivfflat (topic_embedding vector_cosine_ops)
  WITH (lists = 100); -- подбери по объёму

-- Fuzzy текстовый fallback
CREATE INDEX IF NOT EXISTS idx_cpt_topic_trgm
  ON chunking_prompt_templates USING gin (topic gin_trgm_ops);
-- Таблица для хранения пользователей
CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       login      VARCHAR(255) NOT NULL UNIQUE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблица для хранения диалогов (чатов)
CREATE TABLE conversations (
                               id         BIGSERIAL PRIMARY KEY,
    -- Каждый диалог связан с конкретным пользователем
                               user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               title      VARCHAR(255) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- Индекс для быстрого поиска всех диалогов пользователя
CREATE INDEX idx_conversations_user_id ON conversations(user_id);


-- Таблица для хранения сообщений
CREATE TABLE chat_messages (
                               id              BIGSERIAL PRIMARY KEY,
    -- Каждое сообщение связано с конкретным диалогом
                               conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
                               message_type    VARCHAR(20) NOT NULL, -- 'USER' или 'AI'
                               content         TEXT NOT NULL,
                               created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- Индекс для быстрого поиска всех сообщений в диалоге
CREATE INDEX idx_chat_messages_conversation_id ON chat_messages(conversation_id);

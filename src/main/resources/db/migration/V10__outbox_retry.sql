ALTER TABLE chat_message
    ADD COLUMN retry_count       INT          NOT NULL DEFAULT 0,
    ADD COLUMN publish_failed_at TIMESTAMP(3) NULL;

CREATE INDEX idx_chat_message_outbox
    ON chat_message (published_at, publish_failed_at, created_at);

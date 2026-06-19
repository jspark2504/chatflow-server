ALTER TABLE chat_message
    ADD COLUMN client_id VARCHAR(64) NULL,
    ADD CONSTRAINT uq_cm_room_sender_client
        UNIQUE (room_id, sender_id, client_id);

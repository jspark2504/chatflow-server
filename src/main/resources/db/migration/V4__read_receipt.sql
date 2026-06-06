ALTER TABLE chat_room_member
    ADD COLUMN last_read_message_id BIGINT NULL AFTER joined_at,
    ADD CONSTRAINT fk_crm_last_read_message
        FOREIGN KEY (last_read_message_id) REFERENCES chat_message (id);

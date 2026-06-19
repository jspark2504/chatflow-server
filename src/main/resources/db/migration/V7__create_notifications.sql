CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    receiver_id BIGINT       NOT NULL,
    sender_id   BIGINT       NOT NULL,
    room_id     BIGINT       NOT NULL,
    message_id  BIGINT       NOT NULL,
    is_read     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_notifications_message_receiver (message_id, receiver_id),
    INDEX idx_notifications_receiver_unread (receiver_id, is_read)
);

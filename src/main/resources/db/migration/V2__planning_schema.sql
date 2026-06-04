-- 기획문서(DB 테이블정의서) 기준 스키마 정렬 (1단계 users + 2단계 이후 테이블 선생성)

DROP TABLE IF EXISTS user_connection;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_room_member;
DROP TABLE IF EXISTS chat_room;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500) NULL,
    online_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE chat_room (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE chat_room_member (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_crm_room FOREIGN KEY (room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_crm_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_crm_room_user UNIQUE (room_id, user_id)
);

CREATE TABLE chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    message_content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_cm_room FOREIGN KEY (room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_cm_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    INDEX idx_chat_message_room_created (room_id, created_at DESC)
);

CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    content VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_notification_user_created (user_id, created_at DESC)
);

CREATE TABLE user_connection (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    connected_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    disconnected_at TIMESTAMP(3) NULL,
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_user_connection_user (user_id)
);

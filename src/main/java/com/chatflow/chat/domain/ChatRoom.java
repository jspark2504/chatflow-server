package com.chatflow.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_room")
public class ChatRoom {

    @Id
    private Long id;
    private String roomName;

    @Column("room_type")
    private RoomType roomType;

    @Column("created_at")
    private Instant createdAt;

    @Column("last_message_at")
    private Instant lastMessageAt;
}

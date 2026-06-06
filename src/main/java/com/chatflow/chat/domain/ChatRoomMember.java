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
@Table("chat_room_member")
public class ChatRoomMember {

    @Id
    private Long id;

    @Column("room_id")
    private Long roomId;

    @Column("user_id")
    private Long userId;

    @Column("joined_at")
    private Instant joinedAt;

    @Column("last_read_message_id")
    private Long lastReadMessageId;
}

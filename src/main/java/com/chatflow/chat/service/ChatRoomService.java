package com.chatflow.chat.service;

import com.chatflow.chat.domain.ChatRoom;
import com.chatflow.chat.domain.ChatRoomMember;
import com.chatflow.chat.domain.RoomType;
import com.chatflow.chat.dto.CreateRoomRequest;
import com.chatflow.chat.dto.RoomMemberResponse;
import com.chatflow.chat.dto.RoomResponse;
import com.chatflow.chat.repository.ChatRoomMemberRepository;
import com.chatflow.chat.repository.ChatRoomRepository;
import com.chatflow.common.error.BusinessException;
import com.chatflow.user.domain.User;
import com.chatflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatReadService chatReadService;

    public Mono<RoomResponse> createRoom(long requesterId, CreateRoomRequest request) {
        return switch (request.type()) {
            case DIRECT -> createDirectRoom(requesterId, request.targetUserId());
            case GROUP -> createGroupRoom(requesterId, request.roomName(), request.memberUserIds());
        };
    }

    public Flux<RoomResponse> listMyRooms(long userId) {
        return chatRoomRepository.findAllByMemberUserId(userId)
                .flatMap(room -> buildRoomResponse(room, userId));
    }

    public Mono<RoomResponse> getRoom(long roomId, long userId) {
        return requireMember(roomId, userId)
                .then(chatRoomRepository.findById(roomId))
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Chat room not found")))
                .flatMap(room -> buildRoomResponse(room, userId));
    }

    public Mono<Void> requireMember(long roomId, long userId) {
        return chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)
                .flatMap(member -> member
                        ? Mono.empty()
                        : Mono.error(new BusinessException(HttpStatus.FORBIDDEN, "Not a member of this room")));
    }

    private Mono<RoomResponse> createDirectRoom(long requesterId, Long targetUserId) {
        if (targetUserId == null) {
            return Mono.error(new BusinessException(HttpStatus.BAD_REQUEST, "targetUserId is required for DIRECT room"));
        }
        if (requesterId == targetUserId) {
            return Mono.error(new BusinessException(HttpStatus.BAD_REQUEST, "Cannot create direct chat with yourself"));
        }
        return userRepository.findById(targetUserId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Target user not found")))
                .then(Mono.defer(() -> chatRoomRepository.findDirectRoomId(requesterId, targetUserId)
                        .flatMap(chatRoomRepository::findById)
                        .switchIfEmpty(Mono.defer(() -> {
                            Mono<String> roomName = buildDirectRoomName(requesterId, targetUserId);
                            return roomName.flatMap(name -> chatRoomRepository.save(ChatRoom.builder()
                                            .roomName(name)
                                            .roomType(RoomType.DIRECT)
                                            .createdAt(Instant.now())
                                            .build())
                                    .flatMap(room -> addMembers(room.getId(), List.of(requesterId, targetUserId))
                                            .thenReturn(room)));
                        }))))
                .flatMap(room -> buildRoomResponse(room, requesterId));
    }

    private Mono<RoomResponse> createGroupRoom(long requesterId, String roomName, List<Long> memberUserIds) {
        if (roomName == null || roomName.isBlank()) {
            return Mono.error(new BusinessException(HttpStatus.BAD_REQUEST, "roomName is required for GROUP room"));
        }
        Set<Long> memberIds = new HashSet<>();
        memberIds.add(requesterId);
        if (memberUserIds != null) {
            memberIds.addAll(memberUserIds);
        }
        if (memberIds.size() < 2) {
            return Mono.error(new BusinessException(HttpStatus.BAD_REQUEST, "Group room requires at least 2 members"));
        }
        return validateUsersExist(memberIds)
                .then(Mono.defer(() -> chatRoomRepository.save(ChatRoom.builder()
                                .roomName(roomName.trim())
                                .roomType(RoomType.GROUP)
                                .createdAt(Instant.now())
                                .build())
                        .flatMap(room -> addMembers(room.getId(), new ArrayList<>(memberIds)).thenReturn(room))))
                .flatMap(room -> buildRoomResponse(room, requesterId));
    }

    private Mono<Void> validateUsersExist(Set<Long> userIds) {
        return Flux.fromIterable(userIds)
                .flatMap(id -> userRepository.findById(id)
                        .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "User not found: " + id))))
                .then();
    }

    private Mono<Void> addMembers(long roomId, List<Long> userIds) {
        Instant now = Instant.now();
        return Flux.fromIterable(userIds)
                .flatMap(userId -> chatRoomMemberRepository.save(ChatRoomMember.builder()
                        .roomId(roomId)
                        .userId(userId)
                        .joinedAt(now)
                        .build()))
                .then();
    }

    private Mono<String> buildDirectRoomName(long userId1, long userId2) {
        return Flux.just(userId1, userId2)
                .flatMap(userRepository::findById)
                .map(User::getNickname)
                .collectList()
                .map(names -> String.join(" · ", names));
    }

    private Mono<RoomResponse> buildRoomResponse(ChatRoom room, long viewerUserId) {
        if (room.getId() == null) {
            return Mono.error(new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Room id missing"));
        }
        long roomId = room.getId();
        Mono<ChatRoomMember> membership = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, viewerUserId);
        return membership.flatMap(member -> chatRoomMemberRepository.findByRoomId(roomId)
                .flatMap(m -> userRepository.findById(m.getUserId())
                        .map(u -> new RoomMemberResponse(u.getId(), u.getNickname())))
                .collectList()
                .zipWith(chatRoomMemberRepository.countByRoomId(roomId))
                .zipWith(chatReadService.countUnread(roomId, viewerUserId, member.getLastReadMessageId()))
                .map(tuple -> {
                    List<RoomMemberResponse> members = tuple.getT1().getT1();
                    int count = tuple.getT1().getT2().intValue();
                    long unreadCount = tuple.getT2();
                    RoomType type = room.getRoomType() != null
                            ? room.getRoomType()
                            : (count == 2 ? RoomType.DIRECT : RoomType.GROUP);
                    return new RoomResponse(
                            roomId,
                            room.getRoomName(),
                            type,
                            count,
                            unreadCount,
                            member.getLastReadMessageId(),
                            room.getCreatedAt(),
                            members);
                }));
    }
}

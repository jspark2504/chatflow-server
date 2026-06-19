package com.chatflow.status;

import com.chatflow.chat.domain.ChatRoomMember;
import com.chatflow.chat.repository.ChatRoomMemberRepository;
import com.chatflow.infra.security.CurrentUser;
import com.chatflow.websocket.ChatSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class OnlineStatusController {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatSessionRegistry sessionRegistry;

    /**
     * 현재 로그인한 유저와 같은 방에 속한 멤버 중 온라인 상태인 userId 목록 반환.
     */
    @GetMapping("/online-users")
    public Mono<List<Long>> getOnlineUsers() {
        return CurrentUser.auth()
                .flatMap(principal -> {
                    long myUserId = principal.userId();
                    return chatRoomMemberRepository.findByUserId(myUserId)
                            .map(ChatRoomMember::getRoomId)
                            .flatMap(chatRoomMemberRepository::findByRoomId)
                            .map(ChatRoomMember::getUserId)
                            .filter(uid -> uid != myUserId)
                            .collect(Collectors.toSet())
                            .map(contactIds -> new ArrayList<>(
                                    sessionRegistry.getOnlineUserIds(contactIds)));
                });
    }
}

package com.chatflow.status;

import com.chatflow.chat.domain.ChatRoomMember;
import com.chatflow.chat.repository.ChatRoomMemberRepository;
import com.chatflow.infra.security.CurrentUser;
import com.chatflow.redis.RedisPresenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class OnlineStatusController {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final RedisPresenceRepository presenceRepository;

    @GetMapping("/online-users")
    public Mono<List<Long>> getOnlineUsers() {
        return CurrentUser.auth()
                .flatMap(principal -> {
                    long myUserId = principal.userId();
                    log.debug("[online] request myUserId={}", myUserId);
                    return chatRoomMemberRepository.findByUserId(myUserId)
                            .map(ChatRoomMember::getRoomId)
                            .doOnNext(roomId -> log.debug("[online] my roomId={}", roomId))
                            .flatMap(chatRoomMemberRepository::findByRoomId)
                            .map(ChatRoomMember::getUserId)
                            .doOnNext(uid -> log.debug("[online] room member uid={} (me={})", uid, myUserId))
                            .filter(uid -> uid != myUserId)
                            .collect(Collectors.toSet())
                            .flatMap(contactIds -> {
                                log.debug("[online] contactIds={}", contactIds);
                                return presenceRepository.getOnlineUserIds(contactIds);
                            })
                            .map(onlineIds -> {
                                log.debug("[online] onlineIds={}", onlineIds);
                                return new ArrayList<>(onlineIds);
                            });
                });
    }
}

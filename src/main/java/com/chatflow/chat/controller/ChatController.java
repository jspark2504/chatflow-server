package com.chatflow.chat.controller;

import com.chatflow.chat.dto.CreateRoomRequest;
import com.chatflow.chat.dto.MarkReadRequest;
import com.chatflow.chat.dto.MessagePageResponse;
import com.chatflow.chat.dto.MessageResponse;
import com.chatflow.chat.dto.RoomResponse;
import com.chatflow.chat.dto.SendMessageRequest;
import com.chatflow.chat.service.ChatMessageService;
import com.chatflow.chat.service.ChatReadService;
import com.chatflow.chat.service.ChatRoomService;
import com.chatflow.infra.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RoomResponse> createRoom(@Valid @RequestBody Mono<CreateRoomRequest> body) {
        return Mono.zip(body, CurrentUser.auth())
                .flatMap(t -> chatRoomService.createRoom(t.getT2().userId(), t.getT1()));
    }

    @GetMapping("/rooms")
    public Flux<RoomResponse> listRooms() {
        return CurrentUser.auth()
                .flatMapMany(p -> chatRoomService.listMyRooms(p.userId()));
    }

    @GetMapping("/rooms/{roomId:\\d+}")
    public Mono<RoomResponse> getRoom(@PathVariable long roomId) {
        return CurrentUser.auth()
                .flatMap(p -> chatRoomService.getRoom(roomId, p.userId()));
    }

    @PostMapping("/rooms/{roomId:\\d+}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> sendMessage(
            @PathVariable long roomId,
            @Valid @RequestBody Mono<SendMessageRequest> body) {
        return Mono.zip(body, CurrentUser.auth())
                .flatMap(t -> chatMessageService.sendMessage(roomId, t.getT2().userId(), t.getT1()));
    }

    @GetMapping("/rooms/{roomId:\\d+}/messages")
    public Mono<MessagePageResponse> listMessages(
            @PathVariable long roomId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(defaultValue = "50") int size) {
        return CurrentUser.auth()
                .flatMap(p -> chatMessageService.listMessages(roomId, p.userId(), beforeMessageId, size));
    }

    @PutMapping("/rooms/{roomId:\\d+}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> markRead(
            @PathVariable long roomId,
            @Valid @RequestBody Mono<MarkReadRequest> body) {
        return Mono.zip(body, CurrentUser.auth())
                .flatMap(t -> chatReadService.markRead(roomId, t.getT2().userId(), t.getT1().lastReadMessageId()));
    }
}

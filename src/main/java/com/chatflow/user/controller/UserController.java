package com.chatflow.user.controller;

import com.chatflow.infra.security.CurrentUser;
import com.chatflow.user.dto.UserResponse;
import com.chatflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** JWT에 담긴 사용자 정보 반환 (DB 재조회 없음 — 토큰과 동일) */
    @GetMapping("/me")
    public Mono<UserResponse> me() {
        return CurrentUser.auth()
                .map(p -> new UserResponse(p.userId(), p.email(), p.nickname()));
    }

    @GetMapping("/search")
    public Flux<UserResponse> search(@RequestParam String nickname) {
        return userService.searchByNickname(nickname);
    }

    @GetMapping("/{userId:\\d+}")
    public Mono<UserResponse> getUser(@PathVariable long userId) {
        return userService.findById(userId);
    }
}

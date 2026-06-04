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

    @GetMapping("/me")
    public Mono<UserResponse> me() {
        return CurrentUser.auth()
                .flatMap(principal -> userService.me(principal.userId()));
    }

    @GetMapping("/search")
    public Flux<UserResponse> search(@RequestParam String nickname) {
        return userService.searchByNickname(nickname);
    }

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUser(@PathVariable long userId) {
        return userService.findById(userId);
    }
}

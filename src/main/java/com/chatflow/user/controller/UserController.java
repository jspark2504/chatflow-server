package com.chatflow.user.controller;

import com.chatflow.infra.security.CurrentUser;
import com.chatflow.user.dto.LoginRequest;
import com.chatflow.user.dto.LoginResponse;
import com.chatflow.user.dto.RegisterRequest;
import com.chatflow.user.dto.UserResponse;
import com.chatflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> register(@Valid @RequestBody Mono<RegisterRequest> body) {
        return body.flatMap(userService::register);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody Mono<LoginRequest> body) {
        return body.flatMap(userService::login);
    }

    @GetMapping("/me")
    public Mono<UserResponse> me() {
        return CurrentUser.auth()
                .flatMap(principal -> userService.me(principal.userId()));
    }
}

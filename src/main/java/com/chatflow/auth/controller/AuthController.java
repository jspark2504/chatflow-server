package com.chatflow.auth.controller;

import com.chatflow.auth.dto.LoginRequest;
import com.chatflow.auth.dto.LoginResponse;
import com.chatflow.auth.dto.RegisterRequest;
import com.chatflow.auth.service.AuthService;
import com.chatflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> signup(@Valid @RequestBody Mono<RegisterRequest> body) {
        return body.flatMap(authService::signup);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody Mono<LoginRequest> body) {
        return body.flatMap(authService::login);
    }
}

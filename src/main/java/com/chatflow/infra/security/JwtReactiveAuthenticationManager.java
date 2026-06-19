package com.chatflow.infra.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        return jwtService.authenticateToken(token)
                .flatMap(principal -> {
                    Authentication authenticated = new UsernamePasswordAuthenticationToken(
                            principal,
                            token,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    return Mono.just(authenticated);
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid token")))
                .onErrorMap(JwtException.class, ex -> new BadCredentialsException("Invalid token", ex))
                .onErrorMap(ex -> !(ex instanceof AuthenticationException),
                        ex -> new BadCredentialsException("Authentication failed", ex));
    }
}

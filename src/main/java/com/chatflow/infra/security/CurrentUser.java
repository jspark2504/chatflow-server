package com.chatflow.infra.security;

import com.chatflow.common.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Mono<AuthPrincipal> auth() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(AuthPrincipal.class::isInstance)
                .map(AuthPrincipal.class::cast)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized")));
    }
}

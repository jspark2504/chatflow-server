package com.chatflow.infra.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/ws/")) {
            return authenticateWebSocketUpgrade(exchange, chain);
        }
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse());
        }

        String token = header.substring(7);
        try {
            var principal = jwtService.parsePrincipal(token);
            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange.getResponse());
        }
    }

    private Mono<Void> authenticateWebSocketUpgrade(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveTokenFromQueryOrBearer(exchange);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange.getResponse());
        }
        try {
            var principal = jwtService.parsePrincipal(token);
            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange.getResponse());
        }
    }

    private static String resolveTokenFromQueryOrBearer(ServerWebExchange exchange) {
        var query = UriComponentsBuilder.fromUri(exchange.getRequest().getURI()).build().getQueryParams();
        String token = query.getFirst("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        token = query.getFirst("access_token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private static boolean isPublic(String path) {
        return path.startsWith("/api/auth/signup")
                || path.startsWith("/api/auth/login");
    }

    private static Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}

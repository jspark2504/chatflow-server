package com.chatflow.infra.security;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public final class JwtTokenResolver {

    public static final String WEBSOCKET_PROTOCOL_PREFIX = "access_token.";

    private JwtTokenResolver() {
    }

    public static String resolveFromExchange(ServerWebExchange exchange) {
        String fromProtocol = resolveFromSecWebSocketProtocol(
                exchange.getRequest().getHeaders().getFirst("Sec-WebSocket-Protocol"));
        if (StringUtils.hasText(fromProtocol)) {
            return fromProtocol;
        }
        String fromQuery = resolveFromQuery(exchange.getRequest().getURI().getQuery());
        if (StringUtils.hasText(fromQuery)) {
            return fromQuery;
        }
        return resolveFromAuthorizationHeader(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    public static String resolveFromWebSocketSession(WebSocketSession session) {
        String fromProtocol = resolveFromSecWebSocketProtocol(
                session.getHandshakeInfo().getHeaders().getFirst("Sec-WebSocket-Protocol"));
        if (StringUtils.hasText(fromProtocol)) {
            return fromProtocol;
        }
        String fromQuery = resolveFromQuery(session.getHandshakeInfo().getUri().getQuery());
        if (StringUtils.hasText(fromQuery)) {
            return fromQuery;
        }
        return resolveFromAuthorizationHeader(session.getHandshakeInfo().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    public static String resolveFromSecWebSocketProtocol(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        for (String protocol : headerValue.split(",")) {
            String trimmed = protocol.trim();
            if (trimmed.startsWith(WEBSOCKET_PROTOCOL_PREFIX)) {
                String token = trimmed.substring(WEBSOCKET_PROTOCOL_PREFIX.length());
                if (StringUtils.hasText(token)) {
                    return token;
                }
            }
        }
        return null;
    }

    public static boolean hasWebSocketTokenProtocol(List<String> requestedProtocols) {
        if (requestedProtocols == null) {
            return false;
        }
        return requestedProtocols.stream()
                .anyMatch(protocol -> protocol.startsWith(WEBSOCKET_PROTOCOL_PREFIX));
    }

    private static String resolveFromQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return null;
        }
        var params = UriComponentsBuilder.fromUriString("?" + query).build().getQueryParams();
        String token = params.getFirst("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        return params.getFirst("access_token");
    }

    private static String resolveFromAuthorizationHeader(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}

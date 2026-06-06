package com.chatflow.websocket;

import com.chatflow.infra.security.JwtTokenResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Spring WebFlux 6.x에는 DefaultHandshakeHandler가 없어,
 * access_token.{jwt} subprotocol 선택을 HandshakeWebSocketService 확장으로 처리한다.
 */
public class AccessTokenHandshakeWebSocketService extends HandshakeWebSocketService {

    private static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    private static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

    public AccessTokenHandshakeWebSocketService(RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    @Override
    public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        HttpHeaders headers = request.getHeaders();

        if (HttpMethod.GET != method) {
            return Mono.error(new MethodNotAllowedException(
                    request.getMethod(), Collections.singleton(HttpMethod.GET)));
        }
        if (!"WebSocket".equalsIgnoreCase(headers.getUpgrade())) {
            return badRequest(exchange, "Invalid 'Upgrade' header: " + headers);
        }
        List<String> connectionValue = headers.getConnection();
        if (!connectionValue.contains("Upgrade") && !connectionValue.contains("upgrade")) {
            return badRequest(exchange, "Invalid 'Connection' header: " + headers);
        }
        if (headers.getFirst(SEC_WEBSOCKET_KEY) == null) {
            return badRequest(exchange, "Missing \"Sec-WebSocket-Key\" header");
        }

        String protocol = selectProtocol(headers, handler);
        return initHandshakeAttributes(exchange).flatMap(attributes ->
                getUpgradeStrategy().upgrade(exchange, handler, protocol,
                        () -> buildHandshakeInfo(exchange, request, protocol, attributes)));
    }

    @Nullable
    private String selectProtocol(HttpHeaders headers, WebSocketHandler handler) {
        String protocolHeader = headers.getFirst(SEC_WEBSOCKET_PROTOCOL);
        if (protocolHeader == null) {
            return null;
        }
        for (String protocol : StringUtils.commaDelimitedListToStringArray(protocolHeader)) {
            String trimmed = protocol.trim();
            if (trimmed.startsWith(JwtTokenResolver.WEBSOCKET_PROTOCOL_PREFIX)) {
                return trimmed;
            }
        }
        List<String> supportedProtocols = handler.getSubProtocols();
        for (String protocol : StringUtils.commaDelimitedListToStringArray(protocolHeader)) {
            if (supportedProtocols.contains(protocol)) {
                return protocol;
            }
        }
        return null;
    }

    private Mono<Void> badRequest(ServerWebExchange exchange, String reason) {
        return Mono.error(new ServerWebInputException(reason));
    }

    @SuppressWarnings("unchecked")
    private Mono<java.util.Map<String, Object>> initHandshakeAttributes(ServerWebExchange exchange) {
        var predicate = getSessionAttributePredicate();
        if (predicate == null) {
            return Mono.just(Collections.emptyMap());
        }
        return exchange.getSession().map(session ->
                session.getAttributes().entrySet().stream()
                        .filter(entry -> predicate.test(entry.getKey()))
                        .collect(java.util.stream.Collectors.toMap(
                                java.util.Map.Entry::getKey, java.util.Map.Entry::getValue)));
    }

    private org.springframework.web.reactive.socket.HandshakeInfo buildHandshakeInfo(
            ServerWebExchange exchange,
            ServerHttpRequest request,
            @Nullable String protocol,
            java.util.Map<String, Object> attributes) {
        HttpHeaders copiedHeaders = new HttpHeaders();
        copiedHeaders.addAll(request.getHeaders());
        return new org.springframework.web.reactive.socket.HandshakeInfo(
                request.getURI(),
                copiedHeaders,
                request.getCookies(),
                exchange.getPrincipal(),
                protocol,
                request.getRemoteAddress(),
                attributes,
                exchange.getLogPrefix());
    }
}

package com.faforever.gw.messaging;

import com.faforever.gw.messaging.incoming.AckMessage;
import com.faforever.gw.messaging.incoming.ErrorMessage;
import com.faforever.gw.messaging.outgoing.ClientMessage;
import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.GwException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MessagingService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final WebSocketClient webSocketClient;
    private final GwWebSocketHandler webSocketHandler;
    private final ObjectMapper jsonObjectMapper;
    private WebSocketSession currentSession;
    private Map<UUID, CompletableFuture<Void>> pendingRequests;

    @Inject
    public MessagingService(ApplicationEventPublisher applicationEventPublisher, GwWebSocketHandler webSocketHandler, ObjectMapper jsonObjectMapper) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.webSocketHandler = webSocketHandler;
        this.jsonObjectMapper = jsonObjectMapper;
        webSocketClient = new StandardWebSocketClient();
        pendingRequests = new HashMap<>();
    }

    public CompletableFuture<WebSocketSession> connect(String uri) {
        CompletableFuture<WebSocketSession> completableFuture = new CompletableFuture<>();

        webSocketClient.doHandshake(webSocketHandler, uri)
                .addCallback(new ListenableFutureCallback<WebSocketSession>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error("WebSocket handshake failed", throwable);
                        completableFuture.completeExceptionally(throwable);
                    }

                    @Override
                    public void onSuccess(WebSocketSession session) {
                        log.info("WebSocket handshake successful");
                        currentSession = session;
                        completableFuture.complete(session);
                        applicationEventPublisher.publishEvent(ClientState.CONNECTED);
                    }
                });

        return completableFuture;
    }

    public void disconnect() {
        if (currentSession != null && currentSession.isOpen()) {
            try {
                currentSession.close();
                log.info("WebSocket disconnect");
            } catch (IOException e) {
                log.error("Error on closing WebSocket connection", e);
            }
        } else {
            log.warn("WebSocket not connected");
        }
    }


    private TextMessage box(ClientMessage message) throws JsonProcessingException {
        MessageType messageType = MessageType.getByClass(message.getClass());
        return new TextMessage(
                jsonObjectMapper.writeValueAsString(
                        new WebSocketEnvelope(messageType.getName(), jsonObjectMapper.writeValueAsString(message))
                )
        );
    }

    public CompletableFuture<Void> send(ClientMessage message) throws IOException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.runAsync(() -> {
                    try {
                        currentSession.sendMessage(box(message));
                    } catch (JsonProcessingException e) {
                        log.error("Error on converting message to string", e);
                        future.completeExceptionally(e);
                    } catch (IOException e) {
                        log.error("Sending message to server failed", e);
                        future.completeExceptionally(e);
                    }
                }
        );

        pendingRequests.put(message.getRequestId(), future);
        return future;
    }

    @EventListener
    private void onAcknowledged(AckMessage message) {
        val requestId = message.getRequestId();
        val future = pendingRequests.remove(requestId);
        if (future != null) {
            log.debug("Ack success: requestId {}", requestId);
            future.complete(null);
        } else {
            log.error("Ack failed: requestId {} not pending", requestId);
        }
    }

    @EventListener
    private void onError(ErrorMessage message) {
        val requestId = message.getRequestId();
        val future = pendingRequests.remove(message.getRequestId());
        if (future != null) {
            log.error("Error on requestId {}: [{}] {}", requestId, message.getErrorCode(), message.getErrorMessage());
            future.completeExceptionally(
                    new GwException(message.getErrorCode(), message.getErrorMessage()));
        } else {
            log.error("Error failed: requestId {} not pending", requestId);
        }
    }


}

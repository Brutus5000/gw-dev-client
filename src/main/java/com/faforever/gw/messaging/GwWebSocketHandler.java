package com.faforever.gw.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GwWebSocketHandler extends TextWebSocketHandler {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper jsonObjectMapper;
    private final Map<String, ActionFunc> actionMapping = new HashMap<>();

    public GwWebSocketHandler(ApplicationEventPublisher applicationEventPublisher, ObjectMapper jsonObjectMapper) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("Connection established");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketEnvelope envelope;

        try {
            envelope = jsonObjectMapper.readValue(message.getPayload(), WebSocketEnvelope.class);

            if (envelope.getAction() == null)
                throw new IOException();
        } catch (Exception e) {
            log.error("Invalid message envelope. Ignoring message.");
            return;
        }

        val messageClass = MessageType.getByAction(envelope.getAction());
        val convertedMessage = jsonObjectMapper.readValue(envelope.getData(), messageClass);
        log.debug("New message with type {} published", messageClass.getTypeName());
        applicationEventPublisher.publishEvent(convertedMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.debug("Connection closed");
    }

    private interface ActionFunc {
        void processMessage(WebSocketEnvelope envelope);
    }
}

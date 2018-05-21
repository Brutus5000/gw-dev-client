package com.faforever.gw.messaging.outgoing;

import lombok.Value;

import java.util.UUID;

@Value
public class DebugMessage implements ClientMessage {
    private final UUID requestId;
    private final String action;

    public DebugMessage(String action) {
        this.requestId = UUID.randomUUID();
        this.action = action;
    }
}

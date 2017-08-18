package com.faforever.gw.messaging.outgoing;

import lombok.Value;

import java.util.UUID;

@Value
public class InitiateAssaultMessage implements ClientMessage {
    private final UUID requestId;
    private final UUID planetId;

    public InitiateAssaultMessage(UUID planetId) {
        this.requestId = UUID.randomUUID();
        this.planetId = planetId;
    }
}
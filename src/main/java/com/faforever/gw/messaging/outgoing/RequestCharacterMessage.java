package com.faforever.gw.messaging.outgoing;

import com.faforever.gw.model.entitity.Faction;
import lombok.Value;

import java.util.UUID;


@Value
public class RequestCharacterMessage implements ClientMessage {
    private final UUID requestId;
    private final Faction faction;

    public RequestCharacterMessage(Faction faction) {
        this.requestId = UUID.randomUUID();
        this.faction = faction;
    }
}
package com.faforever.gw.messaging.outgoing;

import com.faforever.gw.model.entitity.Faction;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor
public class SetPlanetFactionRequestMessage implements ClientMessage {
    public SetPlanetFactionRequestMessage(UUID planetId, Faction newOwner) {
        this.requestId = UUID.randomUUID();
        this.planetId = planetId;
        this.newOwner = newOwner;
    }

    private UUID requestId;
    private UUID planetId;
    private Faction newOwner;
}

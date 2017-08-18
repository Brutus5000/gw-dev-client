package com.faforever.gw.messaging.outgoing;

import com.faforever.gw.messaging.MessageType;
import com.faforever.gw.model.entitity.Faction;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static com.faforever.gw.messaging.MessageType.ADMIN_SET_PLANET_FACTION_REQUEST;

@Value
@AllArgsConstructor
public class SetPlanetFactionRequestMessage implements ClientMessage {
    private UUID requestId;
    private UUID planetId;
    private Faction newOwner;

    @Override
    public MessageType getAction() {
        return ADMIN_SET_PLANET_FACTION_REQUEST;
    }
}

package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import com.faforever.gw.model.entitity.Faction;
import lombok.Data;

import java.util.UUID;

@Data
public class PlanetOwnerChangedMessage implements GwMessage {
    private UUID solarSystem;
    private Faction newOwner;

    @Override
    public MessageType getAction() {
        return null;
    }
}

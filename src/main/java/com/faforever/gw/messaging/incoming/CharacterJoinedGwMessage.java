package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.model.entitity.Faction;
import lombok.Data;

import java.util.UUID;

@Data
public class CharacterJoinedGwMessage implements GwMessage {
    private UUID character;
    private Faction faction;
    private String name;
}

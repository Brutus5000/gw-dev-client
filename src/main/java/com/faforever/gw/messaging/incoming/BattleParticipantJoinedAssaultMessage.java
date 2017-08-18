package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.model.entitity.Faction;
import lombok.Data;

import java.util.UUID;

@Data
public class BattleParticipantJoinedAssaultMessage implements GwMessage {
    private UUID characterId;
    private UUID battleId;
    private Faction attackingFaction;
    private Faction defendingFaction;
}

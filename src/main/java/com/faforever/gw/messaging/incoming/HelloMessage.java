package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import lombok.Data;

import java.util.UUID;

@Data
public class HelloMessage implements GwMessage {
    private UUID characterId;
    private UUID currentBattleId;
}

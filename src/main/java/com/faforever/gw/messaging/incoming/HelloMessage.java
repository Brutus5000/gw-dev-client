package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class HelloMessage implements GwMessage {
    private UUID characterId;
    private UUID currentBattleId;

    @Override
    public MessageType getAction() {
        return MessageType.HELLO;
    }
}

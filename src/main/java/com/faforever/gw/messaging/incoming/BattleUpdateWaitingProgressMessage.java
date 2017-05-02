package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class BattleUpdateWaitingProgressMessage implements GwMessage {
    private UUID battleId;
    private Double waitingProgress;

    @Override
    public MessageType getAction() {
        return MessageType.BATTLE_WAITING_PROGRESS;
    }
}

package com.faforever.gw.messaging.outgoing;

import lombok.Value;

import java.util.UUID;

@Value
public class LeaveAssaultMessage implements ClientMessage {
    private final UUID requestId;
    private final UUID battleId;

    public LeaveAssaultMessage(UUID battleId) {
        this.requestId = UUID.randomUUID();
        this.battleId = battleId;
    }
}

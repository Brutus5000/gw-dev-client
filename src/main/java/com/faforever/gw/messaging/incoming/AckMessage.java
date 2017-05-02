package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class AckMessage implements GwMessage {
    private UUID requestId;

    @Override
    public MessageType getAction() {
        return MessageType.ACK;
    }
}

package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class ErrorMessage implements GwMessage {
    private UUID requestId;
    private String errorCode;
    private String errorMessage;

    @Override
    public MessageType getAction() {
        return MessageType.ERROR;
    }
}

package com.faforever.gw.messaging.outgoing;

import com.faforever.gw.messaging.MessageType;
import lombok.Value;

import java.util.UUID;


@Value
public class SelectCharacterNameMessage implements ClientMessage {
    private final UUID requestId;
    private final String name;

    @Override
    public MessageType getAction() {
        return MessageType.ACTION_SELECT_CHARACTER_NAME;
    }
}

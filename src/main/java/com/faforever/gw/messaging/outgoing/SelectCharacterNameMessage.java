package com.faforever.gw.messaging.outgoing;

import lombok.Value;

import java.util.UUID;


@Value
public class SelectCharacterNameMessage implements ClientMessage {
    private final UUID requestId;
    private final String name;
}

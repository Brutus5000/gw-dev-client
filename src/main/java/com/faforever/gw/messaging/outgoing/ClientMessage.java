package com.faforever.gw.messaging.outgoing;

import com.faforever.gw.messaging.GwMessage;

import java.util.UUID;

public interface ClientMessage extends GwMessage {
    UUID getRequestId();
}

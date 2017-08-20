package com.faforever.gw.messaging.outgoing;

import lombok.Value;

import java.util.UUID;

@Value
public class LinkSolarSystemsRequestMessage implements ClientMessage {
    private UUID requestId;
    private UUID solarSystemFrom;
    private UUID solarSystemTo;

    public LinkSolarSystemsRequestMessage(UUID solarSystemFrom, UUID solarSystemTo) {
        this.requestId = UUID.randomUUID();
        this.solarSystemFrom = solarSystemFrom;
        this.solarSystemTo = solarSystemTo;
    }
}

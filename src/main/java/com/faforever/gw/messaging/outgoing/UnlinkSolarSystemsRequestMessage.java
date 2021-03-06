package com.faforever.gw.messaging.outgoing;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor
public class UnlinkSolarSystemsRequestMessage implements ClientMessage {
    private UUID requestId;
    private UUID solarSystemFrom;
    private UUID solarSystemTo;

    public UnlinkSolarSystemsRequestMessage(UUID solarSystemFrom, UUID solarSystemTo) {
        this.requestId = UUID.randomUUID();
        this.solarSystemFrom = solarSystemFrom;
        this.solarSystemTo = solarSystemTo;
    }
}

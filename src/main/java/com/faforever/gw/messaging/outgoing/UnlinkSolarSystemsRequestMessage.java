package com.faforever.gw.messaging.outgoing;

import com.faforever.gw.messaging.MessageType;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static com.faforever.gw.messaging.MessageType.ADMIN_UNLINK_SOLAR_SYSTEMS_REQUEST;

@Value
@AllArgsConstructor
public class UnlinkSolarSystemsRequestMessage implements ClientMessage {
    private UUID requestId;
    private UUID solarSystemFrom;
    private UUID solarSystemTo;

    @Override
    public MessageType getAction() {
        return ADMIN_UNLINK_SOLAR_SYSTEMS_REQUEST;
    }
}

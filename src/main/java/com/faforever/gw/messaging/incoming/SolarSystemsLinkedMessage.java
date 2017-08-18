package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class SolarSystemsLinkedMessage implements GwMessage {
    private UUID solarSystemFrom;
    private UUID solarSystemTo;

    @Override
    public MessageType getAction() {
        return null;
    }
}

package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class UserIncomeMessage implements GwMessage {
    private UUID character;
    private Long creditsTotal;
    private Long creditsDelta;

    @Override
    public MessageType getAction() {
        return MessageType.USER_INCOME;
    }
}

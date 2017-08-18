package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import lombok.Data;

import java.util.UUID;

@Data
public class CharacterPromotionMessage implements GwMessage {
    private UUID character;
    private int newRank;
}

package com.faforever.gw.model.entitity;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class BattleParticipant implements Serializable {
    private UUID id;
    private Battle battle;
    private GwCharacter character;
    private BattleRole role;
    private BattleParticipantResult result;

    public Faction getFaction() {
        return character.getFaction();
    }
}

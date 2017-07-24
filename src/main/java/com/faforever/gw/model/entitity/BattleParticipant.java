package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.io.Serializable;

@Data
@Type("battleParticipant")
public class BattleParticipant implements Serializable {
    @Id
    private String id;
    @Relationship("battle")
    private Battle battle;
    @Relationship("character")
    private GwCharacter character;
    private BattleRole role;
    private BattleParticipantResult result;

    public Faction getFaction() {
        return character.getFaction();
    }
}

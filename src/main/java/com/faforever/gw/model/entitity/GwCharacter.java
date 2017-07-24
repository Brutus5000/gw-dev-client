package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Type("gwCharacter")
public class GwCharacter implements Serializable {
    @Id
    private String id;
    private int fafId;
    private String name;
    private Faction faction;
    private Long xp;
    @Relationship("battleParticipantList")
    private List<BattleParticipant> battleParticipantList = new ArrayList<>();
    private GwCharacter killer;
    private Set<GwCharacter> killedBy;
    private Rank rank;
}

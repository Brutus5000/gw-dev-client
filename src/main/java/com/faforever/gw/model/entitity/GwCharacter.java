package com.faforever.gw.model.entitity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class GwCharacter implements Serializable {
    private UUID id;
    private int fafId;
    private String name;
    private Faction faction;
    private Long xp;
    private List<BattleParticipant> battleParticipantList = new ArrayList<>();
    private GwCharacter killer;
    private Set<GwCharacter> killedBy;
    private Rank rank;
}

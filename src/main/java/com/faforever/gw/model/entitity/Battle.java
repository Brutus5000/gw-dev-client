package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@Type("battle")
@AllArgsConstructor
public class Battle implements Serializable {
    @Id
    private String id;
    @Relationship("planet")
    private Planet planet;
    @Relationship("participants")
    private List<BattleParticipant> participants;
    private BattleStatus status;
    private Timestamp initiatedAt;
    private double waitingProgress;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private Faction attackingFaction;
    private Faction defendingFaction;
    private Faction winningFaction;
}

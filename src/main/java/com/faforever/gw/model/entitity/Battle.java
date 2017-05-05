package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Type("battle")
@AllArgsConstructor
public class Battle implements Serializable {
    @Id
    private String id;
    private Planet planet;
    //    private List<BattleParticipant> participants = new ArrayList<>();
    private BattleStatus status;
    private Timestamp initiatedAt;
    private double waitingProgress;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private Faction attackingFaction;
    private Faction defendingFaction;
    private Faction winningFaction;
}

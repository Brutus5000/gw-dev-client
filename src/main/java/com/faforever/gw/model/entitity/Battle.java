package com.faforever.gw.model.entitity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Battle implements Serializable {

    private UUID id;
    private Planet planet;
    private List<BattleParticipant> participants = new ArrayList<>();
    private BattleStatus status;
    private Timestamp initiatedAt;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private Faction attackingFaction;
    private Faction defendingFaction;
    private Faction winningFaction;

    public Battle(UUID id, Planet planet, Faction attackingFaction, Faction defendingFaction) {
        this.id = id;
        this.planet = planet;
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.status = BattleStatus.INITIATED;
        this.initiatedAt = Timestamp.from(Instant.now());
    }
}

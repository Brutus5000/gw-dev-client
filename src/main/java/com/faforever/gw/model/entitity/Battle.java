package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Data
@Type("battle")
@AllArgsConstructor
public class Battle implements Serializable {
    @Id
    private String id;
    private Long fafGameId;
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

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Battle)) return false;
        return Objects.equals(id, ((Battle) o).getId());
    }

    public String toString() {
        return MessageFormat.format("Battle [ID = {0}]", id);
    }
}

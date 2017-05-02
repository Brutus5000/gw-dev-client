package com.faforever.gw.model.entitity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum BattleParticipantResult {
    VICTORY("victory"),
    DEATH("death"),
    RECALL("recall");

    private static final Map<String, BattleParticipantResult> fromName;

    static {
        fromName = new HashMap<>();
        for (BattleParticipantResult BattleParticipantResult : values()) {
            fromName.put(BattleParticipantResult.name, BattleParticipantResult);
        }
    }

    @Getter
    private final String name;

    public static BattleParticipantResult fromString(String string) {
        return fromName.get(string);
    }

}
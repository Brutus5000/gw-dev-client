package com.faforever.gw.model.entitity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum BattleStatus implements Serializable {
    INITIATED("initiated"),
    CANCELED("canceled"),
    RUNNING("running"),
    FINISHED("finished");


    private static final Map<String, BattleStatus> fromName;

    static {
        fromName = new HashMap<>();
        for (BattleStatus battleStatus : values()) {
            fromName.put(battleStatus.name, battleStatus);
        }
    }

    @Getter
    private final String name;

    public static BattleStatus fromString(String string) {
        return fromName.get(string);
    }
}

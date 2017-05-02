package com.faforever.gw.model.entitity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum BattleRole {
    ATTACKER("attacker"),
    DEFENDER("defender");

    private static final Map<String, BattleRole> fromName;

    static {
        fromName = new HashMap<>();
        for (BattleRole BattleRole : values()) {
            fromName.put(BattleRole.name, BattleRole);
        }
    }

    @Getter
    private final String name;

    public static BattleRole fromNameString(String string) {
        return fromName.get(string);
    }
}
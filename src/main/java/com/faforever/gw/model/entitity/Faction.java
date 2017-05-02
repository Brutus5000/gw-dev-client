package com.faforever.gw.model.entitity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum Faction {
    AEON("aeon"),
    CYBRAN("cybran"),
    UEF("uef"),
    SERAPHIM("seraphim");

    private static final Map<String, Faction> fromName;

    static {
        fromName = new HashMap<>();
        for (Faction faction : values()) {
            fromName.put(faction.name, faction);
        }
    }

    @Getter
    private final String name;

    public static Faction fromString(String string) {
        return fromName.get(string);
    }

}
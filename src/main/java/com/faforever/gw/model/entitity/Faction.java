package com.faforever.gw.model.entitity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

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

    @JsonCreator
    public static Faction fromString(String string) {
        return fromName.get(string);
    }

    private final String name;

    @JsonValue
    public String getName() {
        return name;
    }

}
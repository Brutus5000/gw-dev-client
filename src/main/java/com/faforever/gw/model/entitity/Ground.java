package com.faforever.gw.model.entitity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum Ground {
    WATER("water"),
    SOIL("soil"),
    LAVA("lava"),
    DESERT("desert"),
    FROST("frost");

    private static final Map<String, Ground> fromName;

    static {
        fromName = new HashMap<>();
        for (com.faforever.gw.model.entitity.Ground Ground : values()) {
            fromName.put(Ground.name, Ground);
        }
    }

    @Getter
    private final String name;


    public static Ground fromString(String string) {
        return fromName.get(string);
    }

}

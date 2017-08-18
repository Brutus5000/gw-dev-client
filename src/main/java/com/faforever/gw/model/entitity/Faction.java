package com.faforever.gw.model.entitity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum Faction {
    AEON("aeon", Color.SEAGREEN),
    CYBRAN("cybran", Color.MAROON),
    UEF("uef", Color.MEDIUMBLUE),
    SERAPHIM("seraphim", Color.GOLD);

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
    @Getter
    private final Color color;

    @JsonValue
    public String getName() {
        return name;
    }

}
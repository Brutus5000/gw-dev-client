package com.faforever.gw.ui;

import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.entitity.SolarSystem;
import lombok.Getter;

import java.text.MessageFormat;

@Getter
public class UniverseItemAdapter {
    private SolarSystem solarSystem;
    private Planet planet;

    public boolean isSolarSystem() {
        return solarSystem != null;
    }

    public boolean isPlanet() {
        return planet != null;
    }

    public String getName() {
        if (isSolarSystem()) {
            return MessageFormat.format("({0},{1},{2}) {3}", solarSystem.getX(), solarSystem.getY(), solarSystem.getZ(), solarSystem.getId());
        } else {
            return MessageFormat.format("{0}", planet.getId());
        }
    }
}

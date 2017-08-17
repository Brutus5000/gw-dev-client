package com.faforever.gw.ui.universe;

import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.entitity.SolarSystem;
import lombok.Getter;

import java.text.MessageFormat;

@Getter
public class UniverseItemAdapter {
    private SolarSystem solarSystem;
    private Planet planet;

    public UniverseItemAdapter(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    public UniverseItemAdapter(Planet planet) {
        this.planet = planet;
    }

    public boolean isSolarSystem() {
        return solarSystem != null;
    }

    public boolean isPlanet() {
        return planet != null;
    }

    public String getLocation() {
        if (isSolarSystem()) {
            return MessageFormat.format("Solar system at ({0},{1},{2})", solarSystem.getX(), solarSystem.getY(), solarSystem.getZ(), solarSystem.getId());
        } else {
            return MessageFormat.format("Planet on orbit level {0}", planet.getOrbitLevel());
        }
    }

    public String getId() {
        if (isSolarSystem()) {
            return solarSystem.getId();
        } else {
            return planet.getId();
        }
    }

    public String getOwner() {
        if (isSolarSystem()) {
            return "";
        } else {
            return planet.getCurrentOwner().toString();
        }
    }
}

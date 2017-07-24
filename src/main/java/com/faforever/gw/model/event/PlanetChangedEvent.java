package com.faforever.gw.model.event;

import com.faforever.gw.model.entitity.Planet;
import lombok.Value;

@Value
public class PlanetChangedEvent {
    Planet planet;
}

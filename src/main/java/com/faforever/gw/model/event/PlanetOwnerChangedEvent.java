package com.faforever.gw.model.event;

import com.faforever.gw.model.entitity.Faction;
import com.faforever.gw.model.entitity.Planet;
import lombok.Value;

@Value
public class PlanetOwnerChangedEvent {
    Planet planet;
    Faction newOwner;
}

package com.faforever.gw.model.event;

import com.faforever.gw.model.entitity.SolarSystem;
import lombok.Value;

@Value
public class SolarSystemsLinkedEvent {
    SolarSystem from;
    SolarSystem to;
}

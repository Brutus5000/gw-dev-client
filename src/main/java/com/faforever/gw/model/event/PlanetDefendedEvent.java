package com.faforever.gw.model.event;

import com.faforever.gw.model.entitity.Battle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanetDefendedEvent {
    private final Battle battle;
}

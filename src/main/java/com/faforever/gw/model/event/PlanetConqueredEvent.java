package com.faforever.gw.model.event;

import com.faforever.gw.model.entitity.Battle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanetConqueredEvent {
    private final Battle battle;
}

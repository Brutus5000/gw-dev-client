package com.faforever.gw.model.event;

import com.faforever.gw.model.entitity.Battle;
import lombok.Value;

@Value
public class BattleChangedEvent {
    Battle battle;
    boolean newBattle;
}

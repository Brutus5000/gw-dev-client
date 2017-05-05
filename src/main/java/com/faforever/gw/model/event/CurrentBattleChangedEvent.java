package com.faforever.gw.model.event;

import lombok.Data;

import java.util.UUID;

@Data
public class CurrentBattleChangedEvent {
    UUID previousBattleId;
    UUID battleId;
}

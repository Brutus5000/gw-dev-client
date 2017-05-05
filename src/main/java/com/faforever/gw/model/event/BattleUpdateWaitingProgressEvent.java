package com.faforever.gw.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BattleUpdateWaitingProgressEvent {
    private final UUID battleId;
    private double waitingProgress;
}

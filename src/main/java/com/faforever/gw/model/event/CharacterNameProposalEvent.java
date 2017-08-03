package com.faforever.gw.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CharacterNameProposalEvent {
    private UUID requestId;
    private List<String> proposedNamesList;
}

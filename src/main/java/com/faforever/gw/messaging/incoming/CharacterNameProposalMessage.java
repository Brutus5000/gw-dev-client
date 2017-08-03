package com.faforever.gw.messaging.incoming;

import com.faforever.gw.messaging.GwMessage;
import com.faforever.gw.messaging.MessageType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CharacterNameProposalMessage implements GwMessage {
    private UUID requestId;
    private List<String> proposedNamesList;

    @Override
    public MessageType getAction() {
        return MessageType.CHARACTER_NAME_PROPOSAL;
    }
}

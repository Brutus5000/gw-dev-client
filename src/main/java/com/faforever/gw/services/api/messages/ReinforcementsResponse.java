package com.faforever.gw.services.api.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ReinforcementsResponse {
	private final Map<UUID, Integer> reinforcements;
}

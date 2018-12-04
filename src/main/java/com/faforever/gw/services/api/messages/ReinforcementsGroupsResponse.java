package com.faforever.gw.services.api.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReinforcementsGroupsResponse {
	//TODO
//	private final List<Group> groups;
	private Map<UUID, Integer> freeReinforcements;
}

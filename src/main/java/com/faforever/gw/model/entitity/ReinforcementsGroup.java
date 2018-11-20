package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Type("reinforcementsGroup")
public class ReinforcementsGroup {
	@Id
	private String id;
	@Relationship("character")
	private GwCharacter character;
	@Relationship("type")
	private ReinforcementsType type;
	@Relationship("reinforcements")
	private List<ReinforcementsGroupEntry> reinforcements = new ArrayList<>();

}

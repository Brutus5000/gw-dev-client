package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("reinforcementsGroupEntry")
public class ReinforcementsGroupEntry {
	@Id
	private String id;
	@Relationship("group")
	private ReinforcementsGroup group;
	@Relationship("reinforcement")
	private Reinforcement reinforcement;
	private int quantity;
}

package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("reinforcement")
public class Reinforcement {

	@Id
	private String id;
	private ReinforcementsType type;
	@Relationship("unit")
	private Unit unit;
	@Relationship("item")
	private PassiveItem item;
	private long delay;
	private float price;
}

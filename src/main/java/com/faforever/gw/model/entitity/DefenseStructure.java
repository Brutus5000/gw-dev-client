package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("defenseStructure")
public class DefenseStructure {

	@Id
	private String id;
	@Relationship("unit")
	private Unit unit;
	private double prize;
}

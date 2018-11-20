package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("unit")
public class Unit {
	@Id
	private String id;
	private String faUid;
	private String name;
	private Faction faction;
	private TechLevel techLevel;
}

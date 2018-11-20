package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("passiveItem")
public class PassiveItem {
	@Id
	private String id;
	private String name;

}

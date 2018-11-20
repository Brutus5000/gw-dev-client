package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("deployedDefenseStructure")
public class DeployedDefenseStructure {
	@Id
	private String id;
	@Relationship("structure")
	private DefenseStructure structure;
	@Relationship("planet")
	private Planet planet;
	private Faction faction;
	@Relationship("creditJournalEntry")
	private CreditJournalEntry creditJournalEntry;
}

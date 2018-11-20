package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Type("reinforcementsTransaction")
public class ReinforcementsTransaction {
	@Id
	private String id;
	@Relationship("character")
	private GwCharacter character;
	@Relationship("battle")
	private Battle battle;
	@Relationship("creditJournalEntry")
	private CreditJournalEntry creditJournalEntry; // May be null if the reinforcements have been used in a battle
	private Timestamp createdAt;
	@Relationship("reinforcement")
	private Reinforcement reinforcement;
	private int quantity; // Negative quantity indicates spending in battle
}

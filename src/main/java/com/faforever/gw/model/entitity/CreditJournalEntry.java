package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Type("creditJournalEntry")
public class CreditJournalEntry {
	@Id
	private String id;
	@Relationship("character")
	private GwCharacter character;
	@Relationship("battle")
	private Battle battle;
	@Relationship("reinforcementsTransaction")
	private ReinforcementsTransaction reinforcementsTransaction;
	@Relationship("reason")
	private CreditJournalEntryReason reason;
	private double amount;
	private Timestamp createdAt;
}

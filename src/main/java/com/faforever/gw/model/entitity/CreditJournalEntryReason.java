package com.faforever.gw.model.entitity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum CreditJournalEntryReason {
	REGULAR_INCOME("I", "regularIncome"),
	VICTORY("V", "victory"),
	ACU_KILL("K", "acuKill"),
	REINFORCEMENTS("R", "reinforcements"),
	DEFENSE_STRUCTURE("D", "defenseStructure");



	private static final Map<String, CreditJournalEntryReason> fromDbKey;
	private static final Map<String, CreditJournalEntryReason> fromName;

	static {
		fromDbKey = new HashMap<>();
		fromName = new HashMap<>();
		for (CreditJournalEntryReason reason : values()) {
			fromDbKey.put(reason.dbKey, reason);
			fromName.put(reason.name, reason);
		}
	}

	@Getter
	private String dbKey;
	private String name;

	public static CreditJournalEntryReason fromDbString(String string) {
		return fromDbKey.get(string);
	}

	@JsonCreator
	public static CreditJournalEntryReason fromName(String name) {
		return fromName.get(name);
	}

	@JsonValue
	public String getName() {
		return name;
	}
}

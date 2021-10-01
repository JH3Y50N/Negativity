package com.elikill58.negativity.universal;

import java.util.Locale;

public enum CheatKeys implements Comparable<CheatKeys> {

	ALL("ALL"),
	AIR_JUMP("AIRJUMP"),
	AIR_PLACE("AIRPLACE"),
	ANTI_KNOCKBACK("ANTIKNOCKBACK"),
	ANTI_POTION("ANTIPOTION"),
	AUTO_CLICK("AUTOCLICK"),
	AUTO_STEAL("AUTOSTEAL"),
	BLINK("BLINK"),
	CHAT("CHAT"),
	CRITICAL("CRITICAL"),
	ELYTRA_FLY("ELYTRAFLY"),
	FAST_BOW("FASTBOW"),
	FAST_EAT("FASTEAT"),
	FAST_LADDER("FASTLADDER"),
	FAST_PLACE("FASTPLACE"),
	FAST_STAIRS("FASTSTAIRS"),
	FLY("FLY"),
	FORCEFIELD("FORCEFIELD"),
    GROUND_SPOOF("GROUNDSPOOF"),
	INVENTORY_MOVE("INVENTORYMOVE"),
	JESUS("JESUS"),
	NO_FALL("NOFALL"),
	NO_PITCH_LIMIT("NOPITCHLIMIT"),
	NO_SLOW_DOWN("NOSLOWDOWN"),
	NO_WEB("NOWEB"),
	NUKER("NUKER"),
	PINGSPOOF("PINGSPOOF"),
	PHASE("PHASE"),
	REGEN("REGEN"),
	SCAFFOLD("SCAFFOLD"),
	SNEAK("SNEAK"),
	SPEED("SPEED"),
	SPIDER("SPIDER"),
	STEP("STEP"),
	TIMER("TIMER"),
	XRAY("XRAY");
	
	private final String key;
	
	private CheatKeys(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getLowerKey() {
		return key.toLowerCase(Locale.ROOT);
	}
	
	@Override
	public String toString() {
		return getLowerKey();
	}
}

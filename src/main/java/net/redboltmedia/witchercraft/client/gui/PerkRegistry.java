package net.redboltmedia.witchercraft.client.gui;

/**
 * Canonical perk id -> name/color table (plan Section 12).
 *
 * IDs are range-encoded so color = id / 100: 1 = red (Combat), 2 = green
 * (Alchemy), 3 = blue (Signs), 4 = neutral (General); 0 = empty slot. This is
 * the single source of truth shared by the equip screen render, the mutagen
 * synergy count, and (eventually) the HTML node-placer. Keep it in sync with
 * Section 12 when perks are added.
 */
public final class PerkRegistry {
	private PerkRegistry() {
	}

	// Ordered red, green, blue, neutral - the display order of the debug palette.
	public static final int[] IDS = {
			// Combat / RED (15)
			101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115,
			// Alchemy / GREEN (9)
			201, 202, 203, 204, 205, 206, 207, 208, 209,
			// Signs / BLUE (15)
			301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315,
			// General / NEUTRAL (6)
			401, 402, 403, 404, 405, 406};

	public static final String[] NAMES = {
			"AnatomicalKnowledge", "ColdBlood", "CripplingShot", "CripplingStrikes", "CrushingBlows", "DeadlyPrecision", "Defence", "FleetFooted", "FloodOfAnger", "MuscleMemory", "PreciseBlows", "RazorFocus",
			"StrengthTraining", "SunderArmor", "Undying",
			"ClusterBombs", "DelayedRecovery", "Efficiency", "HunterInstinct", "PoisonedBlades", "ProtectiveCoating", "Pyrotechnics", "Refreshment", "SideEffects",
			"AardIntensity", "AxiiIntensity", "Delusion", "Domination", "ExplodingShield", "FarReachingAard", "Firestream", "IgniIntensity", "MagicTrap", "Pyromaniac", "QuenDischarge", "QuenIntensity", "ShockWave",
			"SustainedGlyphs", "YrdenIntensity",
			"BearSchool", "CatSchool", "Gourmet", "GriffinSchool", "SunAndStars", "SurvivalInstinct"};

	public static final int COLOR_RED = 1, COLOR_GREEN = 2, COLOR_BLUE = 3, COLOR_NEUTRAL = 4;

	/** color bucket for an id (0 for empty / unknown). */
	public static int color(int id) {
		return id <= 0 ? 0 : id / 100;
	}

	/** index into IDS/NAMES for an id, or -1. */
	public static int indexOf(int id) {
		for (int i = 0; i < IDS.length; i++)
			if (IDS[i] == id)
				return i;
		return -1;
	}

	/** display name for an id, or "" if empty/unknown. */
	public static String name(int id) {
		int i = indexOf(id);
		return i < 0 ? "" : NAMES[i];
	}

	/** ARGB text tint for a color bucket. */
	public static int tint(int colorBucket) {
		switch (colorBucket) {
			case COLOR_RED:
				return 0xFFFF5555;
			case COLOR_GREEN:
				return 0xFF55FF55;
			case COLOR_BLUE:
				return 0xFF5599FF;
			case COLOR_NEUTRAL:
				return 0xFFCCCCCC;
			default:
				return 0xFF777777;
		}
	}
}

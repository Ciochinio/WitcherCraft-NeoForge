package net.redboltmedia.witchercraft;

/**
 * Perk id helpers: branch colour, name, icon slug and lang keys.
 *
 * IDs are range-encoded so color = id / 100: 1 = red (Combat), 2 = green
 * (Alchemy), 3 = blue (Signs), 4 = neutral (General); 0 = empty slot. Names are
 * not listed here: each perk's Blockly procedure declares its id and name with
 * PerkDefine, collected by PerkCatalog. The tree slots are PerkTree.
 */
public final class PerkRegistry {
	private PerkRegistry() {
	}

	public static final int COLOR_RED = 1, COLOR_GREEN = 2, COLOR_BLUE = 3, COLOR_NEUTRAL = 4;

	/** color bucket for an id (0 for empty / unknown). */
	public static int color(int id) {
		return id <= 0 ? 0 : id / 100;
	}

	/** name from the perk's PerkDefine block, or "" if no perk claims this id. */
	public static String name(int id) {
		return PerkCatalog.name(id);
	}

	/**
	 * Lowercase icon slug for an id, or "" if empty/unknown. Matches the cleaned
	 * flat icon filenames under textures/screens/ (&lt;slug&gt;_notlearned.png,
	 * &lt;slug&gt;_notequipped.png, and &lt;slug&gt;_equipped.png). These names keep
	 * all three states visible to MCreator.
	 */
	public static String slug(int id) {
		return name(id).toLowerCase(java.util.Locale.ROOT);
	}

	/** Lang key for a perk's localized display name ("perk.witchercraft.<slug>.name"), or "" if unknown. */
	public static String nameKey(int id) {
		String slug = slug(id);
		return slug.isEmpty() ? "" : "perk.witchercraft." + slug + ".name";
	}

	/** Lang key for a perk's localized description ("perk.witchercraft.<slug>.desc"), or "" if unknown. */
	public static String descKey(int id) {
		String slug = slug(id);
		return slug.isEmpty() ? "" : "perk.witchercraft." + slug + ".desc";
	}

	/**
	 * Spaced display-name fallback derived from {@link #name}, e.g. "RazorFocus"
	 * -> "Razor Focus". Passed as the translatable fallback text so a missing/lost
	 * lang key (MCreator overwrites en_us.json wholesale on save and does not know
	 * about hand-added keys - see TDD 3.11) renders something readable instead of
	 * the raw dotted key string; not itself localized.
	 */
	public static String fallbackName(int id) {
		String raw = name(id);
		if (raw.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			if (i > 0 && Character.isUpperCase(c))
				sb.append(' ');
			sb.append(c);
		}
		return sb.toString();
	}

	/** ARGB text tint for a color bucket. */
	public static int tint(int colorBucket) {
		// branch colours are edited in equip-grid-placer's Colours panel (PerkEquipLayout.BRANCH_COLORS)
		if (colorBucket >= COLOR_RED && colorBucket <= COLOR_NEUTRAL)
			return PerkEquipLayout.BRANCH_COLORS[colorBucket - 1];
		return 0xFF777777;
	}
}

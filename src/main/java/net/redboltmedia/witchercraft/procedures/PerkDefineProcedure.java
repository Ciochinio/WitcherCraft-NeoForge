package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.PerkCatalog;

/**
 * HAND-MAINTAINED, locked_code=true. First block of every perk procedure:
 * declares which tree slot the perk fills (perkId), its name (icon file and
 * lang key prefix) and whether this player has learned it. See PerkCatalog.
 */
public class PerkDefineProcedure {
	public static void execute(double perkId, String perkName, boolean learned) {
		PerkCatalog.define((int) perkId, perkName, learned);
	}
}

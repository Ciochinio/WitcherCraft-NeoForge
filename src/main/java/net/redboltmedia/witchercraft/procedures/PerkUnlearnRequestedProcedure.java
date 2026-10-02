package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.PerkCatalog;

import net.minecraft.world.entity.Entity;

/**
 * HAND-MAINTAINED, locked_code=true. True only while a perk reset is running
 * for this player (PerkUnlearnAll). The perk procedure then clears its own
 * learned var. Skill-point counters are reset by the caller. See PerkCatalog.
 */
public class PerkUnlearnRequestedProcedure {
	public static boolean execute(Entity entity, double perkId) {
		if (entity == null)
			return false;
		return PerkCatalog.unlearnRequested(entity, (int) perkId);
	}
}

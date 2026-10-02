package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.PerkCatalog;

import net.minecraft.world.entity.Entity;

/**
 * HAND-MAINTAINED, locked_code=true. True only while the server is learning
 * this perk for this player (the tree slot is visible, not yet learned, and a
 * prerequisite is met). The perk procedure still checks and spends the skill
 * point itself. See PerkCatalog.
 */
public class PerkLearnRequestedProcedure {
	public static boolean execute(Entity entity, double perkId) {
		if (entity == null)
			return false;
		return PerkCatalog.learnRequested(entity, (int) perkId);
	}
}

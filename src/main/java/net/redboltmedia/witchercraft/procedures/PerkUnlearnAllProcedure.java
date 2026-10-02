package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.PerkCatalog;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

/**
 * HAND-MAINTAINED, locked_code=true. Clears every perk's learned var by running
 * AllPerks with the unlearn flag set (each perk's "if PerkUnlearnRequested"
 * block). Does not touch skill-point counters, sockets or the recompute; the
 * caller (WitcherLevelReset) does those in Blockly.
 */
public class PerkUnlearnAllProcedure {
	public static void execute(Entity entity) {
		if (entity instanceof Player player)
			PerkCatalog.unlearnAll(player);
	}
}

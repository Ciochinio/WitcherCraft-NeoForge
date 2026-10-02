package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.PerkEquipVars;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

/**
 * HAND-MAINTAINED, locked_code=true. True if the perk sits in any of the 12
 * perk slots. A perk whose tree slot is hidden reads as not socketed.
 */
public class PerkSocketedProcedure {
	public static boolean execute(Entity entity, double perkId) {
		return entity instanceof Player player && PerkEquipVars.isPerkSocketed(player, (int) perkId);
	}
}

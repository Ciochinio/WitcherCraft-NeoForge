package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.Entity;

// LOCKED: hand-written, Blockly has no crit-arrow block.
public class IsCritArrowProcedure {
	public static boolean execute(Entity immediatesourceentity) {
		if (immediatesourceentity == null)
			return false;
		return immediatesourceentity instanceof AbstractArrow _arrow && _arrow.isCritArrow();
	}
}

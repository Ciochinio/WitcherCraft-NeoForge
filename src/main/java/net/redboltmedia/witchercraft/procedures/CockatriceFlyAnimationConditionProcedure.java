package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class CockatriceFlyAnimationConditionProcedure {
	public static boolean execute(double x, double y, double z, Entity entity) {
		if (entity == null)
			return false;
		return !entity.onGround();
	}
}

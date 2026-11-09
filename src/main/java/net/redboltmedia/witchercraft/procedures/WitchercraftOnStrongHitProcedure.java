package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class WitchercraftOnStrongHitProcedure {
	public static void execute(Entity sourceentity) {
		if (sourceentity == null)
			return;
		sourceentity.getPersistentData().putDouble("witchercraftStrengthTrainingTick", 0);
	}
}

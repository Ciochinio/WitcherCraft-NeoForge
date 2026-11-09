package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class WitchercraftOnStrongHitProcedure {
	public static void execute(LevelAccessor world, Entity sourceentity) {
		if (sourceentity == null)
			return;
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining && (sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80))) {
			sourceentity.getPersistentData().putDouble("witchercraftHitStreak", 0);
		}
		sourceentity.getPersistentData().putDouble("witchercraftStrengthTrainingTick", 0);
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkThreeStrikes && sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 3) {
			WitchercraftReadyChimeProcedure.execute(world, sourceentity);
		}
	}
}
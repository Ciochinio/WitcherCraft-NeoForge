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
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCrushingBlows) {
			if ((sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsCharges", 0) > 0 && (sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) <= 120)))) {
				sourceentity.getPersistentData().putDouble("witchercraftCrushingBlowsCharges", (sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsCharges", 0) - 1));
				sourceentity.getPersistentData().putDouble("witchercraftHitStreak", 0);
			} else {
				if (sourceentity.getPersistentData().getDoubleOr("witchercraftHitCrit", 0) == 1) {
					sourceentity.getPersistentData().putDouble("witchercraftCrushingBlowsCharges", 2);
					sourceentity.getPersistentData().putDouble("witchercraftCrushingBlowsTick", (sourceentity.tickCount));
					WitchercraftReadyChimeProcedure.execute(world, sourceentity);
				}
			}
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkThreeStrikes && sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 3) {
			WitchercraftReadyChimeProcedure.execute(world, sourceentity);
		}
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class WitchercraftHitBonusProcedure {
	public static double execute(Entity sourceentity) {
		if (sourceentity == null)
			return 0;
		double bonus = 0;
		bonus = 0;
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining && (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) == 2
				&& (sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0
						&& sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80)))) {
			bonus = bonus + 30;
		}
		return bonus;
	}
}

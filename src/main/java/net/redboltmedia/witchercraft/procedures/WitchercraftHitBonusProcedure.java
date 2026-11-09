package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class WitchercraftHitBonusProcedure {
	public static double execute(Entity sourceentity) {
		if (sourceentity == null)
			return 0;
		double bonus = 0;
		bonus = 0;
		sourceentity.getPersistentData().putString("witchercraftHitBonusLog", "");
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining && (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) == 2
				&& (sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0
						&& sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80)))) {
			if (bonus > 0) {
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
			}
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + "Strength Training +30"));
			bonus = bonus + 30;
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkThreeStrikes
				&& (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) > 0 && sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 0)) {
			if (bonus > 0) {
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
			}
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + "Three Strikes +50"));
			bonus = bonus + 50;
		}
		if (bonus > 0) {
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", (" (" + (sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ")"));
		}
		return bonus;
	}
}

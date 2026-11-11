package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class WitchercraftArrowBonusProcedure {
	public static double execute(Entity immediatesourceentity, Entity sourceentity) {
		if (immediatesourceentity == null || sourceentity == null)
			return 0;
		double bonus = 0;
		bonus = 0;
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkColdBlood && IsCritArrowProcedure.execute(immediatesourceentity)
				&& immediatesourceentity.getPersistentData().getDoubleOr("witchercraftCalmShot", 0) > 0) {
			bonus = bonus + 25;
		}
		return bonus;
	}
}

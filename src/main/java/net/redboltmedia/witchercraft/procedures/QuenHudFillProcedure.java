package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class QuenHudFillProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShieldMax > 0) {
			return entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield / entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShieldMax;
		}
		return 0;
	}
}

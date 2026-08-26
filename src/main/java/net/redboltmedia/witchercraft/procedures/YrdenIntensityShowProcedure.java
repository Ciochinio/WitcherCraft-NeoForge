package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class YrdenIntensityShowProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksYrdenIntensity == true) {
			return false;
		}
		return true;
	}
}
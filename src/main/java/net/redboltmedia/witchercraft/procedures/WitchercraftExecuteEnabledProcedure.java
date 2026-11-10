package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class WitchercraftExecuteEnabledProcedure {
	public static boolean execute(Entity sourceentity) {
		if (sourceentity == null)
			return false;
		return sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkDeadlyPrecision;
	}
}

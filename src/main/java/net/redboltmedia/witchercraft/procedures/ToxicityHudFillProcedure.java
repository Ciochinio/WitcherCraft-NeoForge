package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class ToxicityHudFillProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		return entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity / 100;
	}
}

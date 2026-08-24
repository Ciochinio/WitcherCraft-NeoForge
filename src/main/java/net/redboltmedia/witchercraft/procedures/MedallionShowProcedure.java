package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class MedallionShowProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel >= 0;
	}
}

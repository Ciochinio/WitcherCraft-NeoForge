package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class ToxicityHudOverdoseProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).ToxicityOverdoseThreshold > 0) {
			return entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity >= entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).ToxicityOverdoseThreshold;
		}
		return false;
	}
}

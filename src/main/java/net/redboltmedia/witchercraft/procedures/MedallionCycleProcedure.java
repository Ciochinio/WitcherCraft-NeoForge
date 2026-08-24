package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class MedallionCycleProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftMedallion = (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMedallion + 1) % 2;
			_vars.markSyncDirty();
		}
	}
}

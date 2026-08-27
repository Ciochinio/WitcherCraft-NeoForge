package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class Changeto111Procedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftPerkSocket1 = 111;
			_vars.witchercraftPerkSocket2 = 112;
			_vars.witchercraftMutagenSocket1 = 1;
			_vars.markSyncDirty();
		}
	}
}
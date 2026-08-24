package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class QuenActiveShieldDropProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftQuenShield = 0;
			_vars.markSyncDirty();
		}
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("Active Shield down"), true);
	}
}
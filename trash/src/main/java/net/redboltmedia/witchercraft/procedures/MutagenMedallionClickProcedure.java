package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class MutagenMedallionClickProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("mutagen click"), true);
	}
}
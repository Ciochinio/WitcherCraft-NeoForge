package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class AltQuenCastProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		double xRadius = 0;
		double loop = 0;
		double zRadius = 0;
		double particleAmount = 0;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("ALT QUEN"), false);
	}
}
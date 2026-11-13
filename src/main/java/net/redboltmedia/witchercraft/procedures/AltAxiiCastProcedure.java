package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class AltAxiiCastProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (WitchercraftAxiiPuppetProcedure.execute(entity)) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("ALT AXII - CHANGE SOUND"), false);
			WitchercraftSignPlaySoundProcedure.execute(entity, "entity.illusioner.prepare_mirror", 1);
		} else {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("NO ENEMIES DETECTED"), false);
			AxiiRefundProcedure.execute(world, entity);
			AxiiRefundProcedure.execute(world, entity);
		}
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@EventBusSubscriber
public class Testperk111Procedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("now equipped" + (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 + "" + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2))),
					false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("mutagen" + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket1)), false);
	}
}
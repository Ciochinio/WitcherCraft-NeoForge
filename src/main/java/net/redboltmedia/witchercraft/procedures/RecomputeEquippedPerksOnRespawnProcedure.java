package net.redboltmedia.witchercraft.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class RecomputeEquippedPerksOnRespawnProcedure {
	@SubscribeEvent
	public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
		execute(event, event.getEntity(), event.isEndConquered());
	}

	public static void execute(Entity entity, boolean endconquered) {
		execute(null, entity, endconquered);
	}

	private static void execute(@Nullable Event event, Entity entity, boolean endconquered) {
		if (entity == null)
			return;
		RecomputeEquippedPerksProcedure.execute(entity);
		if (!endconquered) {
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth((float) (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1));
		}
	}
}

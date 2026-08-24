package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class CharacterGuiPassiveStaminaRegenProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static String execute(Entity entity) {
		return execute(null, entity);
	}

	private static String execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return "";
		return new java.text.DecimalFormat("##.##").format((entity instanceof LivingEntity _livingEntityPSR && _livingEntityPSR.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN) ? _livingEntityPSR.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).getValue() : 0)) + "stamina/s";
	}
}
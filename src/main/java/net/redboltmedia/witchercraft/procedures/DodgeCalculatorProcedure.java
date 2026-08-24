package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@EventBusSubscriber
public class DodgeCalculatorProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity());
		}
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		double dodgeRoll = 0;
		dodgeRoll = Mth.nextInt(RandomSource.create(), 1, 100);
		if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("dodge roll:" + dodgeRoll)), false);
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("dodge chance:"
						+ (entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(WitchercraftModAttributes.DODGE_CHANCE) ? _livingEntity3.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).getValue() : 0))),
						false);
		}
		if (!(entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.DODGE_COOLDOWN))
				&& dodgeRoll <= (entity instanceof LivingEntity _livingEntity6 && _livingEntity6.getAttributes().hasAttribute(WitchercraftModAttributes.DODGE_CHANCE)
						? _livingEntity6.getAttribute(WitchercraftModAttributes.DODGE_CHANCE).getValue()
						: 0)) {
			if (event instanceof ICancellableEvent _cancellable) {
				_cancellable.setCanceled(true);
			}
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("Dodged EZ"), false);
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.DODGE_COOLDOWN, 60, 1));
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.DAMAGE_BLOCKED, 3, 0));
		}
	}
}
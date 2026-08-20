package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

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
				_player.sendSystemMessage(Component.literal(("dodge chance:" + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftDodgeChance)), false);
		}
		if (!(entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.DODGE_COOLDOWN)) && dodgeRoll <= entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftDodgeChance) {
			if (event instanceof ICancellableEvent _cancellable) {
				_cancellable.setCanceled(true);
			}
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("Dodged EZ"), false);
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.DODGE_COOLDOWN, 60, 1));
		}
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;

@EventBusSubscriber
public class WitchercraftIncomingDamageProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getSource(), event.getEntity(), event.getSource().getDirectEntity(), event.getSource().getEntity(), event.getAmount());
		}
	}

	public static void execute(LevelAccessor world, DamageSource damagesource, Entity entity, Entity immediatesourceentity, Entity sourceentity, double amount) {
		execute(null, world, damagesource, entity, immediatesourceentity, sourceentity, amount);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, DamageSource damagesource, Entity entity, Entity immediatesourceentity, Entity sourceentity, double amount) {
		if (damagesource == null || entity == null || immediatesourceentity == null || sourceentity == null)
			return;
		double hit = 0;
		double critBonus = 0;
		double oil = 0;
		double r = 0;
		if (sourceentity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)) {
			if (sourceentity == immediatesourceentity && !damagesource.is(DamageTypes.THORNS)) {
				hit = amount + (sourceentity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
						? _livingEntity3.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
						: 0);
				oil = 0;
				if (WitchercraftOilMatchesProcedure.execute(world, entity, sourceentity)) {
					oil = 4 * (1 + (sourceentity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(WitchercraftModAttributes.OIL_DAMAGE)
							? _livingEntity4.getAttribute(WitchercraftModAttributes.OIL_DAMAGE).getValue()
							: 0) * 0.01);
				}
				hit = hit + oil;
				hit = hit * (1 + (sourceentity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
						? _livingEntity5.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
						: 0) * 0.01);
				critBonus = 0;
				if (sourceentity.getPersistentData().getDoubleOr("witchercraftCritTick", 0) == sourceentity.tickCount) {
					critBonus = sourceentity.getPersistentData().getDoubleOr("witchercraftCritBonus", 0);
				}
				sourceentity.getPersistentData().putDouble("witchercraftCritBonus", 0);
				hit = hit * (1 + critBonus);
				if (critBonus > 0 && WitchercraftExecuteEnabledProcedure.execute() && !entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:execute_immune"))) && !(entity instanceof Player)
						&& (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.2) {
					hit = (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 100 + 100;
					if (sourceentity instanceof LivingEntity _livEnt15 && _livEnt15.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
						if (sourceentity instanceof ServerPlayer _player)
							_player.sendSystemMessage(Component.literal("execute!"), false);
					}
				}
				if (event instanceof LivingIncomingDamageEvent _event) {
					_event.setAmount((float) hit);
				}
				if (sourceentity instanceof LivingEntity _livEnt17 && _livEnt17.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
					r = Math.round(amount * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("base " + r / 1000)), false);
					r = Math.round((sourceentity instanceof LivingEntity _livingEntity19 && _livingEntity19.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
							? _livingEntity19.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
							: 0) * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("flat +" + r / 1000)), false);
					r = Math.round(oil * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("oil +" + r / 1000)), false);
					r = Math.round((sourceentity instanceof LivingEntity _livingEntity22 && _livingEntity22.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
							? _livingEntity22.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
							: 0) * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("damage +" + r / 1000 + "%")), false);
					r = Math.round(critBonus * 100 * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("crit +" + r / 1000 + "%")), false);
					r = Math.round(hit * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("= " + r / 1000)), false);
				}
			} else {
				hit = amount;
				critBonus = 0;
				if (IsCritArrowProcedure.execute(immediatesourceentity) && WitchercraftCritConditionProcedure.execute(world, sourceentity)) {
					critBonus = (sourceentity instanceof LivingEntity _livingEntity26 && _livingEntity26.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)
							? _livingEntity26.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue()
							: 0) * 0.01;
					hit = amount * (1 + critBonus);
					if (event instanceof LivingIncomingDamageEvent _event) {
						_event.setAmount((float) hit);
					}
				}
				if (sourceentity instanceof LivingEntity _livEnt27 && _livEnt27.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
					r = Math.round(amount * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("arrow " + r / 1000)), false);
					r = Math.round(critBonus * 100 * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("crit +" + r / 1000 + "%")), false);
					r = Math.round(hit * 1000);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("= " + r / 1000)), false);
				}
			}
		}
	}
}
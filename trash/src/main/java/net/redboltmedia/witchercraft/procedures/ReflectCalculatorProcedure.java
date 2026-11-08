package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;

@EventBusSubscriber
public class ReflectCalculatorProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity(), event.getSource().getEntity(), event.getAmount());
		}
	}

	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity, double amount) {
		execute(null, world, entity, sourceentity, amount);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity, double amount) {
		if (entity == null || sourceentity == null)
			return;
		double dodgeRoll = 0;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("hit" + amount)), false);
			if ((sourceentity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:necrophage"))) || sourceentity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:vampire"))))
					&& entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.BLACK_BLOOD_EFFECT)) {
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("reflect damage" + (amount * ((entity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(WitchercraftModAttributes.REFLECT_DAMAGE)
							? _livingEntity5.getAttribute(WitchercraftModAttributes.REFLECT_DAMAGE).getValue()
							: 0) + 15) * 0.01))), false);
			} else {
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("reflect damage" + (amount * (entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(WitchercraftModAttributes.REFLECT_DAMAGE)
							? _livingEntity7.getAttribute(WitchercraftModAttributes.REFLECT_DAMAGE).getValue()
							: 0) * 0.01))), false);
			}
		}
		if ((sourceentity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:necrophage"))) || sourceentity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:vampire"))))
				&& entity instanceof LivingEntity _livEnt11 && _livEnt11.hasEffect(WitchercraftModMobEffects.BLACK_BLOOD_EFFECT)) {
			{
				Entity _ent = sourceentity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.THORNS)),
							(float) (amount * ((entity instanceof LivingEntity _livingEntity12 && _livingEntity12.getAttributes().hasAttribute(WitchercraftModAttributes.REFLECT_DAMAGE)
									? _livingEntity12.getAttribute(WitchercraftModAttributes.REFLECT_DAMAGE).getValue()
									: 0) + 15) * 0.01));
				}
			}
		} else {
			{
				Entity _ent = sourceentity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.THORNS)),
							(float) (amount * (entity instanceof LivingEntity _livingEntity15 && _livingEntity15.getAttributes().hasAttribute(WitchercraftModAttributes.REFLECT_DAMAGE)
									? _livingEntity15.getAttribute(WitchercraftModAttributes.REFLECT_DAMAGE).getValue()
									: 0) * 0.01));
				}
			}
		}
	}
}
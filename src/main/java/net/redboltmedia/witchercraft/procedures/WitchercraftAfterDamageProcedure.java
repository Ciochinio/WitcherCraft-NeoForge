package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

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

public class WitchercraftAfterDamageProcedure {
	public static void execute(LevelAccessor world, DamageSource damagesource, Entity entity, Entity sourceentity, double amount) {
		if (damagesource == null || entity == null || sourceentity == null)
			return;
		double r = 0;
		if (sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			r = Math.round(amount * 1000);
			if (sourceentity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("after armor " + r / 1000)), false);
		}
		if (sourceentity instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) && sourceentity == (damagesource.getDirectEntity())
				&& (sourceentity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntity5.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0) > 0) {
			if (sourceentity instanceof LivingEntity _entity)
				_entity.setHealth((float) ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + amount
						* (sourceentity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntity7.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0)
						* 0.01));
			if (sourceentity instanceof LivingEntity _livEnt9 && _livEnt9.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
				r = Math.round(amount
						* (sourceentity instanceof LivingEntity _livingEntity10 && _livingEntity10.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntity10.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0)
						* 0.01 * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("life steal +" + r / 1000)), false);
			}
		}
		if (entity instanceof LivingEntity _livEnt12 && _livEnt12.hasEffect(WitchercraftModMobEffects.BLACK_BLOOD_EFFECT)
				&& (sourceentity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:necrophage"))) || sourceentity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:vampire"))))) {
			{
				Entity _ent = sourceentity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.THORNS)), (float) (amount * 0.15));
				}
			}
			if (entity instanceof LivingEntity _livEnt17 && _livEnt17.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
				r = Math.round(amount * 0.15 * 1000);
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("black blood " + r / 1000)), false);
			}
		}
	}
}
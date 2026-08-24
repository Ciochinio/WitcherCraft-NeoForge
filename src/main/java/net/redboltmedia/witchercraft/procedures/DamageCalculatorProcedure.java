package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class DamageCalculatorProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity, double amount) {
		if (entity == null || sourceentity == null)
			return;
		if (entity instanceof LivingEntity _livEntBlk && _livEntBlk.hasEffect(WitchercraftModMobEffects.DAMAGE_BLOCKED)) {
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(WitchercraftModMobEffects.DAMAGE_BLOCKED);
			return;
		}
		double critChanceRoll = 0;
		critChanceRoll = Mth.nextInt(RandomSource.create(), 1, 100);
		if (sourceentity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			if (sourceentity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("crit roll:" + critChanceRoll)), false);
			if (sourceentity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("crit chance:" + (sourceentity instanceof LivingEntity _livingEntitySRCCC && _livingEntitySRCCC.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_CHANCE) ? _livingEntitySRCCC.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).getValue() : 0))), false);
		}
		if (critChanceRoll <= (sourceentity instanceof LivingEntity _livingEntitySRCCC && _livingEntitySRCCC.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_CHANCE) ? _livingEntitySRCCC.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).getValue() : 0)) {
			if (!entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:enderdragon")))) {
				if (sourceentity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("BAZA"
								+ (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0)) * (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01))),
								false);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(
								Component.literal(("CRIT!" + (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0))
										* (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01) * (sourceentity instanceof LivingEntity _livingEntitySRCCD && _livingEntitySRCCD.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntitySRCCD.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0) * 0.01)),
								false);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("Steal:"
								+ (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0)) * (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01)
										* (sourceentity instanceof LivingEntity _livingEntitySRCCD && _livingEntitySRCCD.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntitySRCCD.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0) * 0.01 * (sourceentity instanceof LivingEntity _livingEntityLS && _livingEntityLS.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntityLS.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0) * 0.01)),
								false);
				}
				WitchercraftMod.queueServerWork(1, () -> {
					{
						Entity _ent = entity;
						if (_ent.level() instanceof ServerLevel _serverLevel) {
							_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.PLAYER_ATTACK)), (float) ((amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0))
									* (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01) * (sourceentity instanceof LivingEntity _livingEntitySRCCD && _livingEntitySRCCD.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntitySRCCD.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0) * 0.01));
						}
					}
					if (sourceentity instanceof LivingEntity _entity)
						_entity.setHealth((float) ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1)
								+ (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0)) * (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01)
										* (sourceentity instanceof LivingEntity _livingEntitySRCCD && _livingEntitySRCCD.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntitySRCCD.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0) * 0.01 * (sourceentity instanceof LivingEntity _livingEntityLS && _livingEntityLS.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntityLS.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0) * 0.01));
				});
			}
		} else {
			if (sourceentity instanceof LivingEntity _livEnt14 && _livEnt14.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("Baza" + amount)), false);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("Additional Flat" + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0))), false);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("Combined Damage"
							+ (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0)) * (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01) * 1)),
							false);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("Steal:" + (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0))
							* (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01) * (sourceentity instanceof LivingEntity _livingEntityLS && _livingEntityLS.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntityLS.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0) * 0.01)), false);
			}
			WitchercraftMod.queueServerWork(1, () -> {
				{
					Entity _ent = entity;
					if (_ent.level() instanceof ServerLevel _serverLevel) {
						_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.PLAYER_ATTACK)), (float) ((amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0))
								* (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01) * 1));
					}
				}
				if (sourceentity instanceof LivingEntity _entity)
					_entity.setHealth((float) ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + (amount + (sourceentity instanceof LivingEntity _livingEntitySRCAD && _livingEntitySRCAD.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) ? _livingEntitySRCAD.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue() : 0))
							* (1 + (sourceentity instanceof LivingEntity _livingEntitySRCID && _livingEntitySRCID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) ? _livingEntitySRCID.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue() : 0) * 0.01) * (sourceentity instanceof LivingEntity _livingEntityLS && _livingEntityLS.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL) ? _livingEntityLS.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue() : 0) * 0.01));
			});
		}
	}
}
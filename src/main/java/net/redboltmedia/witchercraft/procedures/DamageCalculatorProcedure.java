package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
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
		double critChanceRoll = 0;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.DAMAGE_BLOCKED)) {
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(WitchercraftModMobEffects.DAMAGE_BLOCKED);
		} else {
			critChanceRoll = Mth.nextInt(RandomSource.create(), 1, 100);
			if (sourceentity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("crit roll:" + critChanceRoll)), false);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("crit chance:" + (sourceentity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_CHANCE)
							? _livingEntity5.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).getValue()
							: 0))), false);
			}
			if (critChanceRoll <= (sourceentity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_CHANCE)
					? _livingEntity7.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).getValue()
					: 0)) {
				if (!entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:enderdragon")))) {
					if (sourceentity instanceof LivingEntity _livEnt9 && _livEnt9.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
						if (sourceentity instanceof ServerPlayer _player)
							_player.sendSystemMessage(Component.literal(("BAZA" + ((amount + (sourceentity instanceof LivingEntity _livingEntity10 && _livingEntity10.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
									? _livingEntity10.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
									: 0))
									* (1 + (sourceentity instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
											? _livingEntity11.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
											: 0) * 0.01)))),
									false);
						if (sourceentity instanceof ServerPlayer _player)
							_player.sendSystemMessage(Component.literal(("CRIT!" + ((amount + (sourceentity instanceof LivingEntity _livingEntity13 && _livingEntity13.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
									? _livingEntity13.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
									: 0))
									* (1 + (sourceentity instanceof LivingEntity _livingEntity14 && _livingEntity14.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
											? _livingEntity14.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
											: 0) * 0.01)
									* (sourceentity instanceof LivingEntity _livingEntity15 && _livingEntity15.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)
											? _livingEntity15.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue()
											: 0)
									* 0.01))), false);
						if (sourceentity instanceof ServerPlayer _player)
							_player.sendSystemMessage(Component.literal(("Steal:" + ((amount + (sourceentity instanceof LivingEntity _livingEntity17 && _livingEntity17.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
									? _livingEntity17.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
									: 0))
									* (1 + (sourceentity instanceof LivingEntity _livingEntity18 && _livingEntity18.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
											? _livingEntity18.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
											: 0) * 0.01)
									* (sourceentity instanceof LivingEntity _livingEntity19 && _livingEntity19.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)
											? _livingEntity19.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue()
											: 0)
									* 0.01
									* (sourceentity instanceof LivingEntity _livingEntity20 && _livingEntity20.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL)
											? _livingEntity20.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue()
											: 0)
									* 0.01))), false);
					}
					WitchercraftMod.queueServerWork(1, () -> {
						{
							Entity _ent = entity;
							if (_ent.level() instanceof ServerLevel _serverLevel) {
								_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.PLAYER_ATTACK)),
										(float) ((amount + (sourceentity instanceof LivingEntity _livingEntity22 && _livingEntity22.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
												? _livingEntity22.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
												: 0))
												* (1 + (sourceentity instanceof LivingEntity _livingEntity23 && _livingEntity23.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
														? _livingEntity23.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
														: 0) * 0.01)
												* (sourceentity instanceof LivingEntity _livingEntity24 && _livingEntity24.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)
														? _livingEntity24.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue()
														: 0)
												* 0.01));
							}
						}
						if (sourceentity instanceof LivingEntity _entity)
							_entity.setHealth((float) ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1)
									+ (amount + (sourceentity instanceof LivingEntity _livingEntity28 && _livingEntity28.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
											? _livingEntity28.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
											: 0))
											* (1 + (sourceentity instanceof LivingEntity _livingEntity29 && _livingEntity29.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
													? _livingEntity29.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
													: 0) * 0.01)
											* (sourceentity instanceof LivingEntity _livingEntity30 && _livingEntity30.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)
													? _livingEntity30.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue()
													: 0)
											* 0.01
											* (sourceentity instanceof LivingEntity _livingEntity31 && _livingEntity31.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL)
													? _livingEntity31.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue()
													: 0)
											* 0.01));
					});
				}
			} else {
				if (sourceentity instanceof LivingEntity _livEnt34 && _livEnt34.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("Baza" + amount)), false);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("Additional Flat" + (sourceentity instanceof LivingEntity _livingEntity36 && _livingEntity36.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
								? _livingEntity36.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
								: 0))), false);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("Combined Damage" + ((amount + (sourceentity instanceof LivingEntity _livingEntity38 && _livingEntity38.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
								? _livingEntity38.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
								: 0))
								* (1 + (sourceentity instanceof LivingEntity _livingEntity39 && _livingEntity39.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
										? _livingEntity39.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
										: 0) * 0.01)
								* 1))), false);
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("Steal:" + ((amount + (sourceentity instanceof LivingEntity _livingEntity41 && _livingEntity41.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
								? _livingEntity41.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
								: 0))
								* (1 + (sourceentity instanceof LivingEntity _livingEntity42 && _livingEntity42.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
										? _livingEntity42.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
										: 0) * 0.01)
								* (sourceentity instanceof LivingEntity _livingEntity43 && _livingEntity43.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL)
										? _livingEntity43.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue()
										: 0)
								* 0.01))), false);
				}
				WitchercraftMod.queueServerWork(1, () -> {
					{
						Entity _ent = entity;
						if (_ent.level() instanceof ServerLevel _serverLevel) {
							_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.PLAYER_ATTACK)),
									(float) ((amount + (sourceentity instanceof LivingEntity _livingEntity45 && _livingEntity45.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
											? _livingEntity45.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
											: 0))
											* (1 + (sourceentity instanceof LivingEntity _livingEntity46 && _livingEntity46.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
													? _livingEntity46.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
													: 0) * 0.01)
											* 1));
						}
					}
					if (sourceentity instanceof LivingEntity _entity)
						_entity.setHealth((float) ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1)
								+ (amount + (sourceentity instanceof LivingEntity _livingEntity50 && _livingEntity50.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
										? _livingEntity50.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
										: 0))
										* (1 + (sourceentity instanceof LivingEntity _livingEntity51 && _livingEntity51.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
												? _livingEntity51.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
												: 0) * 0.01)
										* (sourceentity instanceof LivingEntity _livingEntity52 && _livingEntity52.getAttributes().hasAttribute(WitchercraftModAttributes.LIFE_STEAL)
												? _livingEntity52.getAttribute(WitchercraftModAttributes.LIFE_STEAL).getValue()
												: 0)
										* 0.01));
				});
			}
		}
	}
}
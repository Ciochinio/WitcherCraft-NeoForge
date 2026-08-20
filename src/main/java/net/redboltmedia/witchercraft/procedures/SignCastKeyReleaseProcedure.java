package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class SignCastKeyReleaseProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignNoCast == false) {
			if (!(entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.SIGN_COOLDOWN))) {
				if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.AXII_SIGN) && !entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDelusion) {
					AxiiCastProcedure.execute(world, entity);
					SignCostProcedure.execute(entity);
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 1));
				}
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime > 20) {
					if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) > 4) {
						if (entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.AARD_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFarReachingAard) {
							AltAardCastProcedure.execute(world, x, y, z, entity);
							SignCostProcedure.execute(entity);
							SignCostProcedure.execute(entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						}
						if (entity instanceof LivingEntity _livEnt6 && _livEnt6.hasEffect(WitchercraftModMobEffects.YRDEN_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesMagicTrap) {
							AltYrdenCastProcedure.execute(world, x, y, z, entity);
							SignCostProcedure.execute(entity);
							SignCostProcedure.execute(entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						}
						if (entity instanceof LivingEntity _livEnt8 && _livEnt8.hasEffect(WitchercraftModMobEffects.AXII_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDelusion) {
							AltAxiiCastProcedure.execute(world, entity);
							SignCostProcedure.execute(entity);
							SignCostProcedure.execute(entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						}
					} else if (!(entity instanceof LivingEntity _livEnt10 && _livEnt10.hasEffect(WitchercraftModMobEffects.QUEN_SIGN)) && !(entity instanceof LivingEntity _livEnt11 && _livEnt11.hasEffect(WitchercraftModMobEffects.IGNI_SIGN))) {
						if (entity instanceof ServerPlayer _player)
							_player.sendSystemMessage(Component.literal("Not enough stamina for alternate sign"), true);
					}
					if (entity instanceof LivingEntity _livEnt13 && _livEnt13.hasEffect(WitchercraftModMobEffects.QUEN_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesExploadingShield) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
					}
					if (entity instanceof LivingEntity _livEnt15 && _livEnt15.hasEffect(WitchercraftModMobEffects.IGNI_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFireStream) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
					}
				}
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime <= 20) {
					if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) > 4) {
						if (entity instanceof LivingEntity _livEnt18 && _livEnt18.hasEffect(WitchercraftModMobEffects.YRDEN_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesMagicTrap) {
							SignCostProcedure.execute(entity);
							YrdenCastProcedure.execute(world, x, y, z, entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						}
						if (entity instanceof LivingEntity _livEnt20 && _livEnt20.hasEffect(WitchercraftModMobEffects.QUEN_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesExploadingShield) {
							SignCostProcedure.execute(entity);
							QuenCastProcedure.execute(entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						}
						if (entity instanceof LivingEntity _livEnt22 && _livEnt22.hasEffect(WitchercraftModMobEffects.AARD_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFarReachingAard) {
							SignCostProcedure.execute(entity);
							AardCastProcedure.execute(entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 1));
						}
						if (entity instanceof LivingEntity _livEnt24 && _livEnt24.hasEffect(WitchercraftModMobEffects.IGNI_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFireStream) {
							SignCostProcedure.execute(entity);
							IgniCastProcedure.execute(entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 1));
						}
						if (entity instanceof LivingEntity _livEnt26 && _livEnt26.hasEffect(WitchercraftModMobEffects.AXII_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesDelusion) {
							SignCostProcedure.execute(entity);
							AxiiCastProcedure.execute(world, entity);
							if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 1));
						}
					} else {
						if (entity instanceof ServerPlayer _player)
							_player.sendSystemMessage(Component.literal("Not enough stamina"), true);
					}
				}
			}
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftSignKeyHold = false;
				_vars.witchercraftSignKeyHoldTime = 0;
				_vars.markSyncDirty();
			}
		}
	}
}
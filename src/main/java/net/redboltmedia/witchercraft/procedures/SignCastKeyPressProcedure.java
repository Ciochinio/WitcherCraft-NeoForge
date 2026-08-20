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

public class SignCastKeyPressProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.SIGN_COOLDOWN)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftSignNoCast = true;
				_vars.markSyncDirty();
			}
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("CANT CAST YET"), true);
		} else {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftSignNoCast = false;
				_vars.markSyncDirty();
			}
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_HOLD, 10000, 0));
			if (!(entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(WitchercraftModMobEffects.SIGN_COOLDOWN))) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftSignKeyHold = true;
					_vars.markSyncDirty();
				}
				if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) > 4) {
					if (entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.YRDEN_SIGN) && !entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesMagicTrap) {
						YrdenCastProcedure.execute(world, x, y, z, entity);
						SignCostProcedure.execute(entity);
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						if (entity instanceof LivingEntity _entity)
							_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
					} else if (entity instanceof LivingEntity _livEnt8 && _livEnt8.hasEffect(WitchercraftModMobEffects.QUEN_SIGN) && !entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesExploadingShield) {
						QuenCastProcedure.execute(entity);
						SignCostProcedure.execute(entity);
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
						if (entity instanceof LivingEntity _entity)
							_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
					} else if (entity instanceof LivingEntity _livEnt11 && _livEnt11.hasEffect(WitchercraftModMobEffects.AARD_SIGN) && !entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFarReachingAard) {
						AardCastProcedure.execute(entity);
						SignCostProcedure.execute(entity);
						if (entity instanceof LivingEntity _entity)
							_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
					} else if (entity instanceof LivingEntity _livEnt14 && _livEnt14.hasEffect(WitchercraftModMobEffects.IGNI_SIGN) && !entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFireStream) {
						IgniCastProcedure.execute(entity);
						SignCostProcedure.execute(entity);
						if (entity instanceof LivingEntity _entity)
							_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.SIGN_COOLDOWN, 40, 0));
					}
				} else {
					if (entity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal("Not enough stamina"), true);
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
				}
			}
		}
	}
}
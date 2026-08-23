package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

public class ToxicityTickStartProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double sumPassiveHealthRegeneration = 0;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity > 0) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftToxicity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity - 1;
				_vars.markSyncDirty();
			}
			if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal("tox -1"), false);
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity >= entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicityOverdoseThreshold) {
			{
				Entity _ent = entity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.MAGIC)),
							1 + Math.round(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity / entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicityOverdoseThreshold));
				}
			}
			if (entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(
							Component.literal(("overdose za " + (1 + Math.round(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity / entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicityOverdoseThreshold)))),
							false);
			}
		}
	}
}
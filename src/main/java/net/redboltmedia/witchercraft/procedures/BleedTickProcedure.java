package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

public class BleedTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(WitchercraftModMobEffects.BLEED) ? _livEnt.getEffect(WitchercraftModMobEffects.BLEED).getDuration() : 0) % 20 == 0) {
			{
				Entity _ent = entity;
				if (_ent.level() instanceof ServerLevel _serverLevel) {
					_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.MAGIC)),
							1 + (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.BLEED) ? _livEnt0.getEffect(WitchercraftModMobEffects.BLEED).getAmplifier() : 0));
				}
			}
		}
	}
}

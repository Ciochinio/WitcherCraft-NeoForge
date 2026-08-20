package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

public class BleedEffectStartProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.BLEED)) {
			WitchercraftMod.queueServerWork(20, () -> {
				if (!(entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.BLEED_COOLDOWN))) {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.BLEED_COOLDOWN, 20, 0));
					{
						Entity _ent = entity;
						if (_ent.level() instanceof ServerLevel _serverLevel) {
							_ent.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.MAGIC)), 1);
						}
					}
				}
				BleedEffectStartProcedure.execute(world, entity);
			});
		}
	}
}
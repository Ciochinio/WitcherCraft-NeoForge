package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.procedures.WyvernDecoctionTickProcedure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.server.level.ServerLevel;

public class WyvernDecoctionEffectMobEffect extends MobEffect {
	public WyvernDecoctionEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		WyvernDecoctionTickProcedure.execute(entity);
		return super.applyEffectTick(level, entity, amplifier);
	}
}
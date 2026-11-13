package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.procedures.AxiiCharmedTickProcedure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.server.level.ServerLevel;

public class AxiiCharmedMobEffect extends MobEffect {
	public AxiiCharmedMobEffect() {
		super(MobEffectCategory.HARMFUL, -6737204);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		AxiiCharmedTickProcedure.execute(level, entity);
		return super.applyEffectTick(level, entity, amplifier);
	}
}
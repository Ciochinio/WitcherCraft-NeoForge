package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.procedures.CorrectOilStartProcedure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class CorrectOilMobEffect extends MobEffect {
	public CorrectOilMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		CorrectOilStartProcedure.execute(entity);
	}
}
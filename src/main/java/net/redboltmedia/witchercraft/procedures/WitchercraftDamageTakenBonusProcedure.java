package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class WitchercraftDamageTakenBonusProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		double bonus = 0;
		bonus = 0;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.SUNDERED)) {
			bonus = bonus + 10 * ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(WitchercraftModMobEffects.SUNDERED) ? _livEnt.getEffect(WitchercraftModMobEffects.SUNDERED).getAmplifier() : 0) + 1);
		}
		return bonus;
	}
}

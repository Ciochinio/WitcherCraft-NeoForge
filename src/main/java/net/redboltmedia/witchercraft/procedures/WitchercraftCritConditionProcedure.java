package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class WitchercraftCritConditionProcedure {
	public static boolean execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return false;
		return sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.THUNDERBOLT_EFFECT) && world instanceof Level _lvl1 && _lvl1.isThundering()
				|| entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.YRDEN_TRAPPED) || entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(WitchercraftModMobEffects.YRDEN_ZAPPED)
				|| entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.AXII_CHARMED)
				|| entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.MAIMED) && entity.getPersistentData().getDoubleOr("witchercraftMaimedHits", 0) > 0;
	}
}
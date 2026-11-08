package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class WitchercraftCritConditionProcedure {
	public static boolean execute(LevelAccessor world, Entity sourceentity) {
		if (sourceentity == null)
			return false;
		return sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.THUNDERBOLT_EFFECT) && world instanceof Level _lvl1 && _lvl1.isThundering();
	}
}
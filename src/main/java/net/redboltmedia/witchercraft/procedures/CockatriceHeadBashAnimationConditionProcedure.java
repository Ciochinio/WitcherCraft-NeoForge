package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class CockatriceHeadBashAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.swinging && entity.onGround()) {
			return true;
		}
		return false;
	}
}
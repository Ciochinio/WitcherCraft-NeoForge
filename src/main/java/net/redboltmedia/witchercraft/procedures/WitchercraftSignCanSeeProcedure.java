package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

// Locked: MCreator has no line-of-sight block. Vanilla eye-to-eye ray, solid blocks (glass too) stop it.
public class WitchercraftSignCanSeeProcedure {
	public static boolean execute(Entity entity, Entity target) {
		if (entity == null || target == null)
			return false;
		return entity instanceof LivingEntity _liv && _liv.hasLineOfSight(target);
	}
}

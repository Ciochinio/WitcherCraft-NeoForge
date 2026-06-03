package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class CockatriceWalkAnimationConditionProcedure {
	public static boolean execute(double x, double y, double z, Entity entity) {
		if (entity == null)
			return false;
		return entity.onGround() && isMoving(entity);
	}

	static boolean isMoving(Entity entity) {
		Vec3 movement = entity.getDeltaMovement();
		if (movement.horizontalDistanceSqr() > 1.0E-5) {
			return true;
		}
		return entity instanceof Mob mob && mob.getNavigation().isInProgress();
	}
}

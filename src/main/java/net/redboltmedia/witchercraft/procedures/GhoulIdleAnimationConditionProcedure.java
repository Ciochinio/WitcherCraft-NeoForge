package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.GhoulEntity;
import net.minecraft.world.entity.Entity;

public class GhoulIdleAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		return entity instanceof GhoulEntity ghoul && ghoul.getEntityData().get(GhoulEntity.DATA_AttackAnimation) == 0
				&& ghoul.getDeltaMovement().horizontalDistanceSqr() < 1.0E-5 && ghoul.getNavigation().isDone();
	}
}

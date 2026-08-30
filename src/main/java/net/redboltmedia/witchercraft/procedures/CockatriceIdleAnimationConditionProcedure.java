package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.CockatriceEntity;
import net.minecraft.world.entity.Entity;

public class CockatriceIdleAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		if (!(entity instanceof CockatriceEntity cockatrice))
			return false;
		return !cockatrice.getEntityData().get(CockatriceEntity.DATA_Flying)
				&& cockatrice.getEntityData().get(CockatriceEntity.DATA_AttackAnimation) == 0
				&& cockatrice.getDeltaMovement().horizontalDistanceSqr() < 1.0E-5
				&& cockatrice.getNavigation().isDone();
	}
}

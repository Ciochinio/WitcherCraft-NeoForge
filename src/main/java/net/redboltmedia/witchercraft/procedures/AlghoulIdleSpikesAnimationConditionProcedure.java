package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.AlghoulEntity;
import net.minecraft.world.entity.Entity;

public class AlghoulIdleSpikesAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		return entity instanceof AlghoulEntity alghoul && alghoul.getEntityData().get(AlghoulEntity.DATA_StanceAnimation) == 0
				&& alghoul.getEntityData().get(AlghoulEntity.DATA_AttackAnimation) == 0 && alghoul.getEntityData().get(AlghoulEntity.DATA_SpikesOut)
				&& alghoul.getDeltaMovement().horizontalDistanceSqr() < 1.0E-5 && alghoul.getNavigation().isDone();
	}
}

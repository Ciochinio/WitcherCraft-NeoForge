package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.AlghoulEntity;

import net.minecraft.world.entity.Entity;

public class AlghoulSpikesOnAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return (entity instanceof AlghoulEntity _datEntI ? _datEntI.getEntityData().get(AlghoulEntity.DATA_StanceAnimation) : 0) == 1;
	}
}
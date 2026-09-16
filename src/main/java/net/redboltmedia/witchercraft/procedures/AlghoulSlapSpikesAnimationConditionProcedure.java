package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.AlghoulEntity;

import net.minecraft.world.entity.Entity;

public class AlghoulSlapSpikesAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return (entity instanceof AlghoulEntity _datEntI ? _datEntI.getEntityData().get(AlghoulEntity.DATA_AttackAnimation) : 0) == 1 && entity instanceof AlghoulEntity _datEntL1 && _datEntL1.getEntityData().get(AlghoulEntity.DATA_SpikesOut);
	}
}
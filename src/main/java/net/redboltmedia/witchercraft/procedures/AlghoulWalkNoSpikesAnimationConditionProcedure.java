package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.AlghoulEntity;

import net.minecraft.world.entity.Entity;

public class AlghoulWalkNoSpikesAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return (entity instanceof AlghoulEntity _datEntI ? _datEntI.getEntityData().get(AlghoulEntity.DATA_StanceAnimation) : 0) == 0
				&& (entity instanceof AlghoulEntity _datEntI ? _datEntI.getEntityData().get(AlghoulEntity.DATA_AttackAnimation) : 0) == 0 && !(entity instanceof AlghoulEntity _datEntL2 && _datEntL2.getEntityData().get(AlghoulEntity.DATA_SpikesOut));
	}
}
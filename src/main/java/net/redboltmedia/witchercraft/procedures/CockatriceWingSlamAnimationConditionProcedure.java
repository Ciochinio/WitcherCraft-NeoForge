package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.CockatriceEntity;
import net.minecraft.world.entity.Entity;

public class CockatriceWingSlamAnimationConditionProcedure {
	public static boolean execute(Entity entity) {
		return entity instanceof CockatriceEntity cockatrice
				&& cockatrice.getEntityData().get(CockatriceEntity.DATA_AttackAnimation) == 2;
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class RecomputeEquippedPerksProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		RecomputeEquippedPerksCombatProcedure.execute(entity);
		RecomputeEquippedPerksAlchemyProcedure.execute(entity);
		RecomputeEquippedPerksSignsProcedure.execute(entity);
		RecomputeEquippedPerksGeneralProcedure.execute(entity);
		PerkModifiersProcedure.execute(entity);
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class CraftPotionProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CraftPotionSwallowProcedure.execute(entity);
		CraftPotionPetrisPhilterProcedure.execute(entity);
		CraftPotionWhiteHoneyProcedure.execute(entity);
		CraftPotionKillerWhaleProcedure.execute(entity);
		CraftPotionWhiteRaffardsDecoctionProcedure.execute(entity);
		ThunderboltUsedProcedure.execute(entity);
		CraftPotionFullMoonProcedure.execute(entity);
		CraftPotionBlizzardProcedure.execute(entity);
		CraftPotionGoldenOrioleProcedure.execute(entity);
		CraftPotionCatProcedure.execute(entity);
		CraftPotionTawnyOwlProcedure.execute(entity);
	}
}
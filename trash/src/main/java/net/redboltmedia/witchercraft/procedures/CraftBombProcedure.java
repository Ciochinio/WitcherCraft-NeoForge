package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class CraftBombProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CraftBombSamumProcedure.execute(entity);
		CraftBombDevilsPuffballProcedure.execute(entity);
		CraftBombDimeritiumProcedure.execute(entity);
		CraftBombGrapeshotProcedure.execute(entity);
		CraftBombDancingStarProcedure.execute(entity);
		CraftBombNorthernWindProcedure.execute(entity);
	}
}
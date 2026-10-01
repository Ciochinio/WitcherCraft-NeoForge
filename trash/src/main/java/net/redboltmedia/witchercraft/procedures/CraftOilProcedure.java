package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class CraftOilProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CraftOilInsectoidProcedure.execute(entity);
		CraftOilNecrophageProcedure.execute(entity);
		CraftOilVampireProcedure.execute(entity);
		CraftOilHangedMansVenomProcedure.execute(entity);
		CraftOilSpecterProcedure.execute(entity);
		CraftOilOgroidProcedure.execute(entity);
		CraftOilBeastProcedure.execute(entity);
		CraftOilDraconidProcedure.execute(entity);
		CraftOilRelictProcedure.execute(entity);
		CraftOilCursedProcedure.execute(entity);
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class QuenEffectTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		QuenActiveTickProcedure.execute(entity);
		QuenAuraProcedure.execute(world, entity);
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class ExperienceFixProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		for (int _i1 = 0; _i1 < 50; _i1++) {
			CharacterExperienceCalculatorProcedure.execute(entity, 0);
		}
	}
}
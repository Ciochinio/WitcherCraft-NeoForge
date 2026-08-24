package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class QuenActiveShieldTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield <= 0) {
			QuenActiveShieldEndProcedure.execute(entity);
		} else {
			QuenActiveTickProcedure.execute(entity);
			QuenActiveShieldAuraProcedure.execute(world, entity);
		}
	}
}
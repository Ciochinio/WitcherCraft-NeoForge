package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

public class SignCastHoldCostTickProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.isAlive()
				&& (entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) > entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignHoldCostPerSecond) {
			if (entity instanceof Player _player)
				_player.getFoodData().setFoodLevel(
						(int) ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) - entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignHoldCostPerSecond));
		}
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CharacterAbilitesSkillPointsAvailableProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "SP available: " + new java.text.DecimalFormat("##.##").format(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPlayerLevel - entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesLearned);
	}
}
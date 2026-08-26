package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CharacterAbilitiesCombatGuiSkillPointsUsedProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "Combat SP used: " + new java.text.DecimalFormat("##.##").format(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksCombatSkillPointsUsed);
	}
}
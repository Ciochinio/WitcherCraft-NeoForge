package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CharacterAbilitiesSignsGuiSkillPointsUsedProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "Signs SP used: " + new java.text.DecimalFormat("##.##").format(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksSignsSkillPointsUsed);
	}
}
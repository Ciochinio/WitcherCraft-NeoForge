package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CharacterAbilitiesAlchemyGuiSkillPointsUsedProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "Alchemy SP used: " + new java.text.DecimalFormat("##.##").format(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksAlchemySkillPointsUsed);
	}
}
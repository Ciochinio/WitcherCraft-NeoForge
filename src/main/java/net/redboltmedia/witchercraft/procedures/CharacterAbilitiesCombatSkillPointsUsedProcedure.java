package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CharacterAbilitiesCombatSkillPointsUsedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftPerksCombatSkillPointsUsed = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksCombatSkillPointsUsed + 1;
			_vars.witchercraftPerksLearned = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksLearned + 1;
			_vars.markSyncDirty();
		}
	}
}
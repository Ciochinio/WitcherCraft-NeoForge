package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CripplingShotEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
			if (CharacterAbilitiesCombatTier3Procedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksCripplingShot = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesCombatSkillPointsUsedProcedure.execute(entity);
			}
		}
	}
}
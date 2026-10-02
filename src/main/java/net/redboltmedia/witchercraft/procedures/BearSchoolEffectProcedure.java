package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class BearSchoolEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(401, "BearSchool", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksBearSchool);
		if (PerkLearnRequestedProcedure.execute(entity, 401)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksBearSchool = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesSkillPointUsedProcedure.execute(entity);
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkBearSchool = PerkSocketedProcedure.execute(entity, 401);
			_vars.markSyncDirty();
		}
	}
}

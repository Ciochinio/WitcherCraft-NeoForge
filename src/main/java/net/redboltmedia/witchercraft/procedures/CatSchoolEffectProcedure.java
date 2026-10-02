package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class CatSchoolEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(402, "CatSchool", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksCatSchool);
		if (PerkLearnRequestedProcedure.execute(entity, 402)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksCatSchool = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesSkillPointUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 402)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksCatSchool = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkCatSchool = PerkSocketedProcedure.execute(entity, 402);
			_vars.markSyncDirty();
		}
	}
}

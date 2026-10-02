package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class GriffinSchoolEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(404, "GriffinSchool", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksGriffinSchool);
		if (PerkLearnRequestedProcedure.execute(entity, 404)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksGriffinSchool = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesSkillPointUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 404)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksGriffinSchool = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkGriffinSchool = PerkSocketedProcedure.execute(entity, 404);
			_vars.markSyncDirty();
		}
	}
}

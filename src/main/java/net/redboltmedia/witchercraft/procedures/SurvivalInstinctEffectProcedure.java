package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class SurvivalInstinctEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(406, "SurvivalInstinct", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksSurvivalInstinct);
		if (PerkLearnRequestedProcedure.execute(entity, 406)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksSurvivalInstinct = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesSkillPointUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 406)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksSurvivalInstinct = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkSurvivalInstinct = PerkSocketedProcedure.execute(entity, 406);
			_vars.markSyncDirty();
		}
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class PyrotechnicsEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(207, "Pyrotechnics", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksPyrotechnics);
		if (PerkLearnRequestedProcedure.execute(entity, 207)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksPyrotechnics = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesAlchemySkillPointsUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 207)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksPyrotechnics = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkPyrotechnics = PerkSocketedProcedure.execute(entity, 207);
			_vars.markSyncDirty();
		}
	}
}

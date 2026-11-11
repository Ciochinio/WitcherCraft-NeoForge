package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class MaimingShotEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(103, "MaimingShot", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksMaimingShot);
		if (PerkLearnRequestedProcedure.execute(entity, 103)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksMaimingShot = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesCombatSkillPointsUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 103)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksMaimingShot = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkMaimingShot = PerkSocketedProcedure.execute(entity, 103);
			_vars.markSyncDirty();
		}
	}
}

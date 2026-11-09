package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class ThreeStrikesEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(111, "ThreeStrikes", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksThreeStrikes);
		if (PerkLearnRequestedProcedure.execute(entity, 111)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksThreeStrikes = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesCombatSkillPointsUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 111)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksThreeStrikes = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkThreeStrikes = PerkSocketedProcedure.execute(entity, 111);
			_vars.markSyncDirty();
		}
	}
}

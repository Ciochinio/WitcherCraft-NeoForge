package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class FarReachingAardEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(306, "FarReachingAard", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksFarReachingAard);
		if (PerkLearnRequestedProcedure.execute(entity, 306)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksFarReachingAard = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesSignsSkillPointsUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 306)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksFarReachingAard = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkFarReachingAard = PerkSocketedProcedure.execute(entity, 306);
			_vars.markSyncDirty();
		}
	}
}

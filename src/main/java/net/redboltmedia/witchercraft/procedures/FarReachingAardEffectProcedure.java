package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class FarReachingAardEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksFarReachingAard = true;
				_vars.markSyncDirty();
			}
			CharacterAbilitiesSignsSkillPointsUsedProcedure.execute(entity);
		}
	}
}
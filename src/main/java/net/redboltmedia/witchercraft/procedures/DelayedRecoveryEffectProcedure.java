package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class DelayedRecoveryEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
			if (CharacterAbilitiesAlchemyTier2Procedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksDelayedRecovery = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesAlchemySkillPointsUsedProcedure.execute(entity);
			}
		}
	}
}
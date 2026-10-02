package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class DelayedRecoveryEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(202, "DelayedRecovery", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksDelayedRecovery);
		if (PerkLearnRequestedProcedure.execute(entity, 202)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksDelayedRecovery = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesAlchemySkillPointsUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 202)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksDelayedRecovery = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkDelayedRecovery = PerkSocketedProcedure.execute(entity, 202);
			_vars.markSyncDirty();
		}
	}
}

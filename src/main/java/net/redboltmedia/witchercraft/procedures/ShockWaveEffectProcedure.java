package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class ShockWaveEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(313, "ShockWave", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksShockWave);
		if (PerkLearnRequestedProcedure.execute(entity, 313)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksShockWave = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesSignsSkillPointsUsedProcedure.execute(entity);
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkShockWave = PerkSocketedProcedure.execute(entity, 313);
			_vars.markSyncDirty();
		}
	}
}

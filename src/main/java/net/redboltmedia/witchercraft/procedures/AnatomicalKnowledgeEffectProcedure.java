package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class AnatomicalKnowledgeEffectProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		PerkDefineProcedure.execute(101, "AnatomicalKnowledge", entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksAnatomicalKnowledge);
		if (PerkLearnRequestedProcedure.execute(entity, 101)) {
			if (CharacterAbilitiesSkillPointCheckProcedure.execute(entity)) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftPerksAnatomicalKnowledge = true;
					_vars.markSyncDirty();
				}
				CharacterAbilitiesCombatSkillPointsUsedProcedure.execute(entity);
			}
		}
		if (PerkUnlearnRequestedProcedure.execute(entity, 101)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPerksAnatomicalKnowledge = false;
				_vars.markSyncDirty();
			}
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftEquippedPerkAnatomicalKnowledge = PerkSocketedProcedure.execute(entity, 101);
			_vars.markSyncDirty();
		}
	}
}

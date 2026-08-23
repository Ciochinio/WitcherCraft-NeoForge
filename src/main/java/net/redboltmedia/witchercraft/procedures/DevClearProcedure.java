package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class DevClearProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftAbilitiesGourmet = false;
			_vars.witchercraftAbilitiesSurvivalInstinct = false;
			_vars.witchercraftAbilitiesSunAndStars = false;
			_vars.witchercraftAbilitiesCatSchool = false;
			_vars.witchercraftAbilitiesGriffinSchool = false;
			_vars.witchercraftAbilitiesBearSchool = false;
			_vars.witchercraftAbilitiesRefreshment = false;
			_vars.witchercraftAbilitiesDelayedRecovery = false;
			_vars.witchercraftAbilitiesSideEffects = false;
			_vars.witchercraftAbilitiesPoisonedBlades = false;
			_vars.witchercraftAbilitiesProtectiveCoating = false;
			_vars.witchercraftAbilitiesHunterInstinct = false;
			_vars.witchercraftAbilitiesPyrotechnics = false;
			_vars.witchercraftAbilitiesEfficiency = false;
			_vars.witchercraftAbilitiesClusterBombs = false;
			_vars.witchercraftAbilitiesFarReachingAard = false;
			_vars.witchercraftAbilitiesAardIntensity = false;
			_vars.witchercraftAbilitiesShockWave = false;
			_vars.witchercraftAbilitiesFirestream = false;
			_vars.witchercraftAbilitiesIgniIntensity = false;
			_vars.witchercraftAbilitiesPyromaniac = false;
			_vars.witchercraftAbilitiesSustainedGlyphs = false;
			_vars.witchercraftAbilitiesYrdenIntensity = false;
			_vars.witchercraftAbilitiesMagicTrap = false;
			_vars.witchercraftAbilitiesExplodingShield = false;
			_vars.witchercraftAbilitiesQuenIntensity = false;
			_vars.witchercraftAbilitiesQuenDischarge = false;
			_vars.witchercraftAbilitiesDelusion = false;
			_vars.witchercraftAbilitiesAxiiIntensity = false;
			_vars.witchercraftAbilitiesDomination = false;
			_vars.witchercraftAbilitiesMuscleMemory = false;
			_vars.witchercraftAbilitiesPreciseBlows = false;
			_vars.witchercraftAbilitiesCripplingStrikes = false;
			_vars.witchercraftAbilitiesStrengthTraining = false;
			_vars.witchercraftAbilitiesCrushingBlows = false;
			_vars.witchercraftAbilitiesSunderArmor = false;
			_vars.witchercraftAbilitiesFleetFooted = false;
			_vars.witchercraftAbilitiesDefence = false;
			_vars.witchercraftAbilitiesDeadlyPrecision = false;
			_vars.witchercraftAbilitiesColdBlood = false;
			_vars.witchercraftAbilitiesAnatomicalKnowledge = false;
			_vars.witchercraftAbilitiesCripplingShot = false;
			_vars.witchercraftAbilitiesFloodOfAnger = false;
			_vars.witchercraftAbilitiesRazorFocus = false;
			_vars.witchercraftAbilitiesUndying = false;
			_vars.witchercraftAbilitiesCombatSkillPointsUsed = 0;
			_vars.witchercraftAbilitiesAlchemySkillPointsUsed = 0;
			_vars.witchercraftAbilitiesSignsSkillPointsUsed = 0;
			_vars.witchercraftAbilitiesLearned = 0;
			_vars.witchercraftPlayerLevel = 0;
			_vars.witchercraftPlayerExperience = 0;
			_vars.witchercraftPlayerExperienceRequirement = 100;
			_vars.markSyncDirty();
		}
	}
}
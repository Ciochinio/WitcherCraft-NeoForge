package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class DevClearProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftPerksGourmet = false;
			_vars.witchercraftPerksSurvivalInstinct = false;
			_vars.witchercraftPerksSunAndStars = false;
			_vars.witchercraftPerksCatSchool = false;
			_vars.witchercraftPerksGriffinSchool = false;
			_vars.witchercraftPerksBearSchool = false;
			_vars.witchercraftPerksRefreshment = false;
			_vars.witchercraftPerksDelayedRecovery = false;
			_vars.witchercraftPerksSideEffects = false;
			_vars.witchercraftPerksPoisonedBlades = false;
			_vars.witchercraftPerksProtectiveCoating = false;
			_vars.witchercraftPerksHunterInstinct = false;
			_vars.witchercraftPerksPyrotechnics = false;
			_vars.witchercraftPerksEfficiency = false;
			_vars.witchercraftPerksClusterBombs = false;
			_vars.witchercraftPerksFarReachingAard = false;
			_vars.witchercraftPerksAardIntensity = false;
			_vars.witchercraftPerksShockWave = false;
			_vars.witchercraftPerksFirestream = false;
			_vars.witchercraftPerksIgniIntensity = false;
			_vars.witchercraftPerksPyromaniac = false;
			_vars.witchercraftPerksSustainedGlyphs = false;
			_vars.witchercraftPerksYrdenIntensity = false;
			_vars.witchercraftPerksMagicTrap = false;
			_vars.witchercraftPerksExplodingShield = false;
			_vars.witchercraftPerksQuenIntensity = false;
			_vars.witchercraftPerksQuenDischarge = false;
			_vars.witchercraftPerksDelusion = false;
			_vars.witchercraftPerksAxiiIntensity = false;
			_vars.witchercraftPerksDomination = false;
			_vars.witchercraftPerksMuscleMemory = false;
			_vars.witchercraftPerksPreciseBlows = false;
			_vars.witchercraftPerksCripplingStrikes = false;
			_vars.witchercraftPerksStrengthTraining = false;
			_vars.witchercraftPerksCrushingBlows = false;
			_vars.witchercraftPerksSunderArmor = false;
			_vars.witchercraftPerksFleetFooted = false;
			_vars.witchercraftPerksDefence = false;
			_vars.witchercraftPerksDeadlyPrecision = false;
			_vars.witchercraftPerksColdBlood = false;
			_vars.witchercraftPerksAnatomicalKnowledge = false;
			_vars.witchercraftPerksCripplingShot = false;
			_vars.witchercraftPerksFloodOfAnger = false;
			_vars.witchercraftPerksRazorFocus = false;
			_vars.witchercraftPerksUndying = false;
			_vars.witchercraftPerksCombatSkillPointsUsed = 0;
			_vars.witchercraftPerksAlchemySkillPointsUsed = 0;
			_vars.witchercraftPerksSignsSkillPointsUsed = 0;
			_vars.witchercraftPerksLearned = 0;
			_vars.witchercraftPlayerLevel = 0;
			_vars.witchercraftPlayerExperience = 0;
			_vars.witchercraftPlayerExperienceRequirement = 100;
			_vars.markSyncDirty();
		}
	}
}
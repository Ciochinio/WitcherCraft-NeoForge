package net.redboltmedia.witchercraft;

import net.minecraft.world.entity.player.Player;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

/**
 * Read the per-perk "learned" boolean vars (witchercraftPerks&lt;Name&gt;) by
 * perk id, for tree state + prerequisite checks. Reads are safe on either side
 * (the vars sync + persist). Writing/spending is left to the existing
 * &lt;Perk&gt;Effect buy procedures - this is read-only.
 */
public final class PerkLearnedVars {
	private PerkLearnedVars() {
	}

	public static boolean isLearned(Player e, int perkId) {
		WitchercraftModVariables.PlayerVariables v = e.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		switch (perkId) {
			case 101: return v.witchercraftPerksAnatomicalKnowledge;
			case 102: return v.witchercraftPerksColdBlood;
			case 103: return v.witchercraftPerksCripplingShot;
			case 104: return v.witchercraftPerksCripplingStrikes;
			case 105: return v.witchercraftPerksCrushingBlows;
			case 106: return v.witchercraftPerksDeadlyPrecision;
			case 107: return v.witchercraftPerksDefence;
			case 108: return v.witchercraftPerksFleetFooted;
			case 109: return v.witchercraftPerksFloodOfAnger;
			case 110: return v.witchercraftPerksMuscleMemory;
			case 111: return v.witchercraftPerksPreciseBlows;
			case 112: return v.witchercraftPerksRazorFocus;
			case 113: return v.witchercraftPerksStrengthTraining;
			case 114: return v.witchercraftPerksSunderArmor;
			case 115: return v.witchercraftPerksUndying;
			case 201: return v.witchercraftPerksClusterBombs;
			case 202: return v.witchercraftPerksDelayedRecovery;
			case 203: return v.witchercraftPerksEfficiency;
			case 204: return v.witchercraftPerksHunterInstinct;
			case 205: return v.witchercraftPerksPoisonedBlades;
			case 206: return v.witchercraftPerksProtectiveCoating;
			case 207: return v.witchercraftPerksPyrotechnics;
			case 208: return v.witchercraftPerksRefreshment;
			case 209: return v.witchercraftPerksSideEffects;
			case 301: return v.witchercraftPerksAardIntensity;
			case 302: return v.witchercraftPerksAxiiIntensity;
			case 303: return v.witchercraftPerksDelusion;
			case 304: return v.witchercraftPerksDomination;
			case 305: return v.witchercraftPerksExplodingShield;
			case 306: return v.witchercraftPerksFarReachingAard;
			case 307: return v.witchercraftPerksFirestream;
			case 308: return v.witchercraftPerksIgniIntensity;
			case 309: return v.witchercraftPerksMagicTrap;
			case 310: return v.witchercraftPerksPyromaniac;
			case 311: return v.witchercraftPerksQuenDischarge;
			case 312: return v.witchercraftPerksQuenIntensity;
			case 313: return v.witchercraftPerksShockWave;
			case 314: return v.witchercraftPerksSustainedGlyphs;
			case 315: return v.witchercraftPerksYrdenIntensity;
			case 401: return v.witchercraftPerksBearSchool;
			case 402: return v.witchercraftPerksCatSchool;
			case 403: return v.witchercraftPerksGourmet;
			case 404: return v.witchercraftPerksGriffinSchool;
			case 405: return v.witchercraftPerksSunAndStars;
			case 406: return v.witchercraftPerksSurvivalInstinct;
			default: return false;
		}
	}
}

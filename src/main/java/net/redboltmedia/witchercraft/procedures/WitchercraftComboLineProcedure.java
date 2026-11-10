package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;

public class WitchercraftComboLineProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		String line = "";
		String icon = "";
		String lit = "";
		double parts = 0;
		double sig = 0;
		double n = 0;
		line = "";
		parts = 0;
		sig = 0;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkThreeStrikes && (entity.getPersistentData().getDoubleOr("witchercraftLastTypedHitTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftLastTypedHitTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftLastTypedHitTick", 0) <= 80))) {
			icon = "";
			lit = "";
			if (entity.getPersistentData().getDoubleOr("witchercraftLastHitType", 0) == 2) {
				icon = "";
				lit = "";
			}
			if (entity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 1) {
				if (parts > 0) {
					line = line + "   ";
				}
				line = line + ("§f" + icon + " §8" + icon + " " + icon);
				parts = parts + 1;
				sig = sig + (entity.getPersistentData().getDoubleOr("witchercraftLastHitType", 0) * 3 + 1);
			}
			if (entity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 2) {
				if (parts > 0) {
					line = line + "   ";
				}
				line = line + ("§f" + icon + " " + icon + " §8" + icon);
				parts = parts + 1;
				sig = sig + (entity.getPersistentData().getDoubleOr("witchercraftLastHitType", 0) * 3 + 2);
			}
			if (entity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 3) {
				if (parts > 0) {
					line = line + "   ";
				}
				line = line + ("§f" + lit);
				parts = parts + 1;
				sig = sig + (entity.getPersistentData().getDoubleOr("witchercraftLastHitType", 0) * 3 + 3);
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining && (entity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80)) || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCrushingBlows && (entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsCharges", 0) > 0 && (entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) <= 120)))) {
			n = 0;
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining && (entity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80))) {
				n = n + 1;
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCrushingBlows && (entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsCharges", 0) > 0 && (entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) <= 120)))) {
				n = n + entity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsCharges", 0);
			}
			if (n == 1) {
				if (parts > 0) {
					line = line + "   ";
				}
				line = line + "\u00A7f\uE7A4";
				parts = parts + 1;
				sig = sig + 10;
			}
			if (n == 2) {
				if (parts > 0) {
					line = line + "   ";
				}
				line = line + "\u00A7f\uE7A4 \u00A7f\uE7A4";
				parts = parts + 1;
				sig = sig + 20;
			}
			if (n > 2) {
				if (parts > 0) {
					line = line + "   ";
				}
				line = line + "\u00A7f\uE7A4 \u00A7f\uE7A4 \u00A7f\uE7A4";
				parts = parts + 1;
				sig = sig + 30;
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCounterattack && (entity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) > 0 && (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) >= 0 && entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) <= 60))) {
			if (parts > 0) {
				line = line + "   ";
			}
			line = line + "\u00A7f\uE7A5";
			parts = parts + 1;
			sig = sig + 1000;
		}
		if (sig != entity.getPersistentData().getDoubleOr("witchercraftComboSig", 0)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftComboLine = line;
				_vars.markSyncDirty();
			}
			entity.getPersistentData().putDouble("witchercraftComboSig", sig);
		}
	}
}
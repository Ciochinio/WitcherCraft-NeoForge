package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Entity;

import java.util.Comparator;

public class WitchercraftHitBonusProcedure {
	public static double execute(LevelAccessor world, Entity sourceentity) {
		if (sourceentity == null)
			return 0;
		double bonus = 0;
		double foes = 0;
		bonus = 0;
		sourceentity.getPersistentData().putString("witchercraftHitBonusLog", "");
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining && (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) == 2 && (sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80)))) {
			if (bonus > 0) {
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
			}
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + "Strength Training +30"));
			bonus = bonus + 30;
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkThreeStrikes && (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) > 0 && sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 0)) {
			if (bonus > 0) {
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
			}
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + "Three Strikes +50"));
			bonus = bonus + 50;
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCrushingBlows && (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) == 2 && (sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsCharges", 0) > 0 && (sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCrushingBlowsTick", 0) <= 120))))) {
			if (bonus > 0) {
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
			}
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + "Crushing Blows +40"));
			bonus = bonus + 40;
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkFloodOfAnger) {
			foes = 0;
			{
				final Vec3 _center = new Vec3((sourceentity.getX()), (sourceentity.getY()), (sourceentity.getZ()));
				for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
					if (entityiterator instanceof Monster && entityiterator.isAlive()) {
						foes = foes + 1;
					}
				}
			}
			if (foes > 4) {
				foes = 4;
			}
			if (foes > 1) {
				if (bonus > 0) {
					sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
				}
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ("Flood of Anger +" + Math.round(10 * (foes - 1)))));
				bonus = bonus + 10 * (foes - 1);
			}
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCounterattack && (sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0) > 0 && (sourceentity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) <= 60)))) {
			if (bonus > 0) {
				sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ", "));
			}
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", ((sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + "Counterattack +50"));
			bonus = bonus + 50;
		}
		if (bonus > 0) {
			sourceentity.getPersistentData().putString("witchercraftHitBonusLog", (" (" + (sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", "")) + ")"));
		}
		return bonus;
	}
}

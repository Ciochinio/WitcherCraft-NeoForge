package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

public class WitchercraftOnFastHitProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCounterattack && (sourceentity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftCounterattackTick", 0) <= 60))) {
			sourceentity.getPersistentData().putDouble("witchercraftCounterattackTick", 0);
			sourceentity.getPersistentData().putDouble("witchercraftHitStreak", 0);
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkStrengthTraining) {
			if (!(sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) > 0 && (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) >= 0 && sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftStrengthTrainingTick", 0) <= 80))) {
				WitchercraftReadyChimeProcedure.execute(world, sourceentity);
			}
			sourceentity.getPersistentData().putDouble("witchercraftStrengthTrainingTick", (sourceentity.tickCount));
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkThreeStrikes && sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) % 4 == 3) {
			WitchercraftReadyChimeProcedure.execute(world, sourceentity);
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCripplingStrikes && sourceentity.getPersistentData().getDoubleOr("witchercraftHitCrit", 0) == 1) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.CRIPPLED, 60, 0));
		}
	}
}

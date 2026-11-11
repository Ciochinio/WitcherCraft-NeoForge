package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;

public class AllPerksProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		BloodyRageEffectProcedure.execute(entity);
		ColdBloodEffectProcedure.execute(entity);
		MaimingShotEffectProcedure.execute(entity);
		CripplingStrikesEffectProcedure.execute(entity);
		CounterattackEffectProcedure.execute(entity);
		CrushingBlowsEffectProcedure.execute(entity);
		DeadlyPrecisionEffectProcedure.execute(entity);
		FleetFootedEffectProcedure.execute(entity);
		FloodOfAngerEffectProcedure.execute(entity);
		MuscleMemoryEffectProcedure.execute(entity);
		ThreeStrikesEffectProcedure.execute(entity);
		ArrowDeflectionEffectProcedure.execute(entity);
		StrengthTrainingEffectProcedure.execute(entity);
		SunderArmorEffectProcedure.execute(entity);
		UndyingEffectProcedure.execute(entity);
		ClusterBombsEffectProcedure.execute(entity);
		DelayedRecoveryEffectProcedure.execute(entity);
		EfficiencyEffectProcedure.execute(entity);
		HunterInstinctEffectProcedure.execute(entity);
		PoisonedBladesEffectProcedure.execute(entity);
		ProtectiveCoatingEffectProcedure.execute(entity);
		PyrotechnicsEffectProcedure.execute(entity);
		RefreshmentEffectProcedure.execute(entity);
		SideEffectsEffectProcedure.execute(entity);
		AardIntensityEffectProcedure.execute(entity);
		AxiiIntensityEffectProcedure.execute(entity);
		DelusionEffectProcedure.execute(entity);
		DominationEffectProcedure.execute(entity);
		ExplodingShieldEffectProcedure.execute(entity);
		FarReachingAardEffectProcedure.execute(entity);
		FirestreamEffectProcedure.execute(entity);
		IgniIntensityEffectProcedure.execute(entity);
		MagicTrapEffectProcedure.execute(entity);
		PyromaniacEffectProcedure.execute(entity);
		QuenDischargeEffectProcedure.execute(entity);
		QuenIntensityEffectProcedure.execute(entity);
		ShockWaveEffectProcedure.execute(entity);
		SustainedGlyphsEffectProcedure.execute(entity);
		YrdenIntensityEffectProcedure.execute(entity);
		BearSchoolEffectProcedure.execute(entity);
		CatSchoolEffectProcedure.execute(entity);
		GourmetEffectProcedure.execute(entity);
		GriffinSchoolEffectProcedure.execute(entity);
		SunAndStarsEffectProcedure.execute(entity);
		SurvivalInstinctEffectProcedure.execute(entity);
	}
}

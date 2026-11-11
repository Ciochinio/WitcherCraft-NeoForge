package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

public class WitchercraftOnArrowHitProcedure {
	public static void execute(Entity entity, Entity immediatesourceentity, Entity sourceentity) {
		if (entity == null || immediatesourceentity == null || sourceentity == null)
			return;
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkMaimingShot && IsCritArrowProcedure.execute(immediatesourceentity)) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.MAIMED, 80, 0));
			entity.getPersistentData().putDouble("witchercraftMaimedHits", 2);
		}
	}
}

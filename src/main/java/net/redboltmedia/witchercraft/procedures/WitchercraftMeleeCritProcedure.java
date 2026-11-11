package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class WitchercraftMeleeCritProcedure {
	@SubscribeEvent
	public static void onPlayerCriticalHit(CriticalHitEvent event) {
		execute(event, event.getEntity().level(), event.getTarget(), event.getEntity(), event.isVanillaCritical());
	}

	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity, boolean isvanillacritical) {
		execute(null, world, entity, sourceentity, isvanillacritical);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity, boolean isvanillacritical) {
		if (entity == null || sourceentity == null)
			return;
		double critBonus = 0;
		double hitType = 0;
		if (sourceentity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)) {
			critBonus = 0;
			if (isvanillacritical) {
				critBonus = critBonus + 0.5;
			}
			if (IsFullyChargedAttackProcedure.execute(sourceentity) && (WitchercraftCritConditionProcedure.execute(world, sourceentity)
					|| entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.MAIMED) && entity.getPersistentData().getDoubleOr("witchercraftMaimedHits", 0) > 0)) {
				critBonus = critBonus
						+ (sourceentity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntity1.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0)
								* 0.01;
			}
			if (IsFullyChargedAttackProcedure.execute(sourceentity) && entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.MAIMED) && entity.getPersistentData().getDoubleOr("witchercraftMaimedHits", 0) > 0) {
				entity.getPersistentData().putDouble("witchercraftMaimedHits", (entity.getPersistentData().getDoubleOr("witchercraftMaimedHits", 0) - 1));
				if (entity.getPersistentData().getDoubleOr("witchercraftMaimedHits", 0) <= 0) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(WitchercraftModMobEffects.MAIMED);
				}
			}
			hitType = 0;
			if (IsFullyChargedAttackProcedure.execute(sourceentity)) {
				if (isvanillacritical) {
					hitType = 2;
				} else {
					hitType = 1;
				}
			}
			if (critBonus > 0) {
				if (event instanceof CriticalHitEvent _event) {
					_event.setDamageMultiplier(1);
				}
			}
			sourceentity.getPersistentData().putDouble("witchercraftCritBonus", critBonus);
			sourceentity.getPersistentData().putDouble("witchercraftHitType", hitType);
			sourceentity.getPersistentData().putDouble("witchercraftHitTick", (sourceentity.tickCount));
		}
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;

@EventBusSubscriber
public class WitchercraftDamageCalculatorProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getSource(), event.getEntity(), event.getSource().getDirectEntity(), event.getSource().getEntity(), event.getAmount());
		}
	}

	public static void execute(LevelAccessor world, DamageSource damagesource, Entity entity, Entity immediatesourceentity, Entity sourceentity, double amount) {
		execute(null, world, damagesource, entity, immediatesourceentity, sourceentity, amount);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, DamageSource damagesource, Entity entity, Entity immediatesourceentity, Entity sourceentity, double amount) {
		if (damagesource == null || entity == null || immediatesourceentity == null || sourceentity == null)
			return;
		double hit = 0;
		double critBonus = 0;
		double oil = 0;
		double hitType = 0;
		double hitBonus = 0;
		double takenBonus = 0;
		double dealtMult = 0;
		double executed = 0;
		double normalHit = 0;
		dealtMult = 1;
		if (sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.CRIPPLED)) {
			dealtMult = 0.8;
		}
		if (sourceentity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)) {
			if (sourceentity == immediatesourceentity && !damagesource.is(DamageTypes.THORNS)) {
				critBonus = 0;
				hitType = 0;
				if (sourceentity.getPersistentData().getDoubleOr("witchercraftHitTick", 0) == sourceentity.tickCount) {
					critBonus = sourceentity.getPersistentData().getDoubleOr("witchercraftCritBonus", 0);
					hitType = sourceentity.getPersistentData().getDoubleOr("witchercraftHitType", 0);
				}
				sourceentity.getPersistentData().putDouble("witchercraftCritBonus", 0);
				sourceentity.getPersistentData().putDouble("witchercraftHitType", hitType);
				if (hitType > 0) {
					if (hitType == sourceentity.getPersistentData().getDoubleOr("witchercraftLastHitType", 0) && (sourceentity.getPersistentData().getDoubleOr("witchercraftLastTypedHitTick", 0) > 0
							&& (sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftLastTypedHitTick", 0) >= 0
									&& sourceentity.tickCount - sourceentity.getPersistentData().getDoubleOr("witchercraftLastTypedHitTick", 0) <= 80))) {
						sourceentity.getPersistentData().putDouble("witchercraftHitStreak", (sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0) + 1));
					} else {
						sourceentity.getPersistentData().putDouble("witchercraftHitStreak", 1);
					}
					sourceentity.getPersistentData().putDouble("witchercraftLastHitType", hitType);
					sourceentity.getPersistentData().putDouble("witchercraftLastTypedHitTick", (sourceentity.tickCount));
				}
				if (hitType == 2 && critBonus > 0.5 || hitType != 2 && critBonus > 0) {
					sourceentity.getPersistentData().putDouble("witchercraftHitCrit", 1);
				} else {
					sourceentity.getPersistentData().putDouble("witchercraftHitCrit", 0);
				}
				hit = amount + (sourceentity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
						? _livingEntity3.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
						: 0);
				oil = 0;
				if (WitchercraftOilMatchesProcedure.execute(world, entity, sourceentity)) {
					oil = 4 * (1 + (sourceentity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(WitchercraftModAttributes.OIL_DAMAGE)
							? _livingEntity4.getAttribute(WitchercraftModAttributes.OIL_DAMAGE).getValue()
							: 0) * 0.01);
				}
				hit = hit + oil;
				hitBonus = WitchercraftHitBonusProcedure.execute(world, sourceentity);
				takenBonus = WitchercraftDamageTakenBonusProcedure.execute(entity);
				hit = hit * (1 + ((sourceentity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
						? _livingEntity5.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
						: 0) + hitBonus + takenBonus) * 0.01);
				hit = hit * (1 + critBonus);
				hit = hit * dealtMult;
				if (hitType == 2 && WitchercraftExecuteEnabledProcedure.execute(sourceentity) && !entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:execute_immune"))) && !(entity instanceof Player)
						&& (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.2) {
					normalHit = hit;
					hit = 9999;
					executed = 1;
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.mace.smash_ground_heavy")), SoundSource.PLAYERS, 1, (float) 1.2);
						} else {
							_level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.mace.smash_ground_heavy")), SoundSource.PLAYERS, 1, (float) 1.2, false);
						}
					}
				}
				entity.getPersistentData().putDouble("witchercraftLifeStealCap", (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1));
				if (executed == 1) {
					entity.getPersistentData().putDouble("witchercraftLifeStealCap", Mth.clamp(normalHit, 0, entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1));
				}
				if (event instanceof LivingIncomingDamageEvent _event) {
					_event.setAmount((float) hit);
				}
				sourceentity.getPersistentData().putDouble("witchercraftHitType", 0);
				WitchercraftDamageLogProcedure.execute(sourceentity, 1, amount, hit, critBonus, oil, hitBonus, takenBonus, hitType, executed);
				if (hitType > 0) {
					if (hitType == 1) {
						WitchercraftOnFastHitProcedure.execute(world, entity, sourceentity);
					} else {
						WitchercraftOnStrongHitProcedure.execute(world, entity, sourceentity);
					}
				}
			} else {
				hit = amount;
				critBonus = 0;
				if (IsCritArrowProcedure.execute(immediatesourceentity) && WitchercraftCritConditionProcedure.execute(world, sourceentity)) {
					critBonus = (sourceentity instanceof LivingEntity _livingEntity26 && _livingEntity26.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE)
							? _livingEntity26.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue()
							: 0) * 0.01;
					hit = amount * (1 + critBonus);
				}
				hitBonus = WitchercraftArrowBonusProcedure.execute(immediatesourceentity, sourceentity);
				takenBonus = WitchercraftDamageTakenBonusProcedure.execute(entity);
				hit = hit * (1 + (hitBonus + takenBonus) * 0.01);
				hit = hit * dealtMult;
				if (event instanceof LivingIncomingDamageEvent _event) {
					_event.setAmount((float) hit);
				}
				WitchercraftDamageLogProcedure.execute(sourceentity, 2, amount, hit, critBonus, 0, hitBonus, takenBonus, 0, 0);
			}
		} else {
			if (dealtMult < 1) {
				if (event instanceof LivingIncomingDamageEvent _event) {
					_event.setAmount((float) (amount * dealtMult));
				}
			}
		}
	}
}

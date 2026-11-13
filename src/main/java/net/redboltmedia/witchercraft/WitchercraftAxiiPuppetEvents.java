package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Alt Axii puppet behaviour (AxiiPuppet effect, applied by WitchercraftAxiiPuppet):
 * - each tick the puppet fights the nearest other hostile (Enemy) within 16 blocks; with none around it stands frozen
 * - it can't pick or hurt anything that isn't a hostile mob (players, pets, villagers)
 * - any player's hit ends it; the end runs after the damage, so that hit is still a Precise hit
 * - when it ends (timer, hit, replaced) the fight it started is called off: see endFight
 * Driven from the entity tick rather than the effect tick: adding/removing effects inside an effect tick
 * mutates the effect map vanilla is iterating.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public class WitchercraftAxiiPuppetEvents {
	private static final double RANGE = 16;
	private static final int IDLE_STUN_TICKS = 6;
	private static final double END_RANGE = 24;

	@SubscribeEvent
	public static void onTick(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide() || !mob.hasEffect(WitchercraftModMobEffects.AXII_PUPPET))
			return;
		LivingEntity target = mob.getTarget();
		if (!isFoe(mob, target)) {
			target = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(RANGE), e -> isFoe(mob, e)).stream()
					.min((a, b) -> Double.compare(a.distanceToSqr(mob), b.distanceToSqr(mob))).orElse(null);
			mob.setTarget(target);
		}
		if (target == null) {
			mob.getNavigation().stop();
			mob.addEffect(new MobEffectInstance(WitchercraftModMobEffects.AXII_STUNNED, IDLE_STUN_TICKS, 0, false, false));
		} else {
			MobEffectInstance stun = mob.getEffect(WitchercraftModMobEffects.AXII_STUNNED);
			if (stun != null && stun.getDuration() <= IDLE_STUN_TICKS)
				mob.removeEffect(WitchercraftModMobEffects.AXII_STUNNED);
		}
	}

	@SubscribeEvent
	public static void onChangeTarget(LivingChangeTargetEvent event) {
		LivingEntity next = event.getNewAboutToBeSetTarget();
		if (next != null && event.getEntity().hasEffect(WitchercraftModMobEffects.AXII_PUPPET) && !(next instanceof Enemy && next != event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		Entity attacker = event.getSource().getEntity();
		if (attacker instanceof LivingEntity puppet && puppet.hasEffect(WitchercraftModMobEffects.AXII_PUPPET) && !(event.getEntity() instanceof Enemy))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onDamaged(LivingDamageEvent.Post event) {
		LivingEntity victim = event.getEntity();
		if (victim.level().isClientSide() || !(event.getSource().getEntity() instanceof Player) || !victim.hasEffect(WitchercraftModMobEffects.AXII_PUPPET))
			return;
		victim.removeEffect(WitchercraftModMobEffects.AXII_PUPPET);
		victim.removeEffect(WitchercraftModMobEffects.AXII_CHARMED);
	}

	@SubscribeEvent
	public static void onEffectRemoved(MobEffectEvent.Remove event) {
		if (event.getEffect().value() == WitchercraftModMobEffects.AXII_PUPPET.get())
			endFight(event.getEntity());
	}

	@SubscribeEvent
	public static void onEffectExpired(MobEffectEvent.Expired event) {
		if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().value() == WitchercraftModMobEffects.AXII_PUPPET.get())
			endFight(event.getEntity());
	}

	// Without this the fight outlives the spell: the mobs it hit keep their grudge (hurt-by memory, alerted packs)
	// until one side dies, so the duration and Intensity scaling stop mattering.
	private static void endFight(LivingEntity ex) {
		if (ex.level().isClientSide())
			return;
		if (ex instanceof Mob mob && !(mob.getTarget() instanceof Player))
			mob.setTarget(null);
		if (!(ex.getLastHurtByMob() instanceof Player))
			ex.setLastHurtByMob(null);
		for (Mob other : ex.level().getEntitiesOfClass(Mob.class, ex.getBoundingBox().inflate(END_RANGE), e -> e != ex)) {
			if (other.getTarget() == ex)
				other.setTarget(null);
			if (other.getLastHurtByMob() == ex)
				other.setLastHurtByMob(null);
		}
	}

	private static boolean isFoe(Mob puppet, LivingEntity e) {
		return e != null && e != puppet && e instanceof Enemy && e.isAlive() && e.distanceToSqr(puppet) <= RANGE * RANGE;
	}
}

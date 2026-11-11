/*
 * The code of this mod element is always locked.
 *
 * HAND-MAINTAINED (locked code element WitchercraftAttackWindup, ~/Admin/Calc). Mob attack wind-up
 * (GDD 5, Blocking and parrying). Vanilla melee mobs swing and deal damage in the same call
 * (MeleeAttackGoal.checkAndPerformAttack), so there is nothing to react to. For mobs in the
 * witchercraft:telegraphed_attackers tag the hit on a player is cancelled at the start, which leaves
 * the vanilla swing and the crosshair indicator (WitchercraftParryIndicatorMessage) as the tells, and the real attack is made WINDUP_TICKS later through the mob's own
 * doHurtTarget, so knockback, on-hit effects, shields, parries, Quen and the damage procedures all apply
 * as usual. Blockly cannot cancel and re-issue a mob's attack, hence Java.
 */

package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.procedures.WitchercraftParryIndicatorEnabledProcedure;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import java.util.WeakHashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@EventBusSubscriber
public final class WitchercraftAttackWindup {
	public static final TagKey<EntityType<?>> TELEGRAPHED = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:telegraphed_attackers"));
	/** About 0.4 s. The crosshair indicator closes over exactly this long; the 0.25 s perfect window means the raise has to be timed into its last part. */
	public static final int WINDUP_TICKS = 8;
	/** After a player blocks a mob's melee hit, that mob's hits on players are cancelled this long: normal block, perfect parry. */
	public static final int BLOCK_STAGGER_TICKS = 10;
	public static final int PARRY_STAGGER_TICKS = 20;
	/** Push on the attacker after a perfect parry. A plain (non-sprint) player hit is 0.4. */
	public static final float PARRY_KNOCKBACK = 0.3f;

	/** Server thread only. Game time until which the mob can't hit a player. */
	private static final Map<Mob, Long> STAGGERED = new WeakHashMap<>();

	private record Pending(LivingEntity target, long fireAt) {
	}

	/** Server thread only. Weak keys, so a mob that unloads mid wind-up is simply forgotten. */
	private static final Map<Mob, Pending> PENDING = new WeakHashMap<>();
	/** True while we make the delayed attack ourselves, so that hit is not delayed again. */
	private static boolean releasing = false;

	private WitchercraftAttackWindup() {
	}

	/** HIGHEST, so the cancelled first hit never reaches the damage procedures, Quen or the In Combat effect. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (releasing)
			return;
		if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level))
			return;
		if (!(event.getSource().getEntity() instanceof Mob mob) || event.getSource().getDirectEntity() != mob)
			return;
		// the vanilla attack goal has already reset its cooldown and swung, so a staggered mob loses this attack
		Long staggeredUntil = STAGGERED.get(mob);
		if (staggeredUntil != null && level.getGameTime() < staggeredUntil) {
			event.setCanceled(true);
			trace(player, name(mob) + " is staggered, swings at air");
			return;
		}
		if (!WorldMapServerConfig.attackWindup() || !mob.is(TELEGRAPHED))
			return;
		event.setCanceled(true);
		if (PENDING.containsKey(mob))
			return;
		PENDING.put(mob, new Pending(player, level.getGameTime() + WINDUP_TICKS));
		if (player instanceof ServerPlayer serverPlayer && WitchercraftParryIndicatorEnabledProcedure.execute(serverPlayer))
			PacketDistributor.sendToPlayer(serverPlayer, new WitchercraftParryIndicatorMessage(mob.getId(), WINDUP_TICKS));
		trace(player, name(mob) + " winds up");
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (PENDING.isEmpty())
			return;
		List<Map.Entry<Mob, Pending>> due = new ArrayList<>();
		Iterator<Map.Entry<Mob, Pending>> it = PENDING.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Mob, Pending> entry = it.next();
			Mob mob = entry.getKey();
			if (mob == null || mob.isRemoved()) {
				it.remove();
			} else if (mob.level().getGameTime() >= entry.getValue().fireAt()) {
				due.add(Map.entry(mob, entry.getValue()));
				it.remove();
			}
		}
		for (Map.Entry<Mob, Pending> entry : due) {
			Mob mob = entry.getKey();
			LivingEntity target = entry.getValue().target();
			// a step back out of reach during the wind-up dodges the attack
			if (!mob.isAlive() || !target.isAlive() || mob.level() != target.level() || !(mob.level() instanceof ServerLevel level))
				continue;
			if (!mob.isWithinMeleeAttackRange(target)) {
				trace(target, name(mob) + " misses (out of reach)");
				continue;
			}
			// the vanilla swing played at the start of the wind-up and is over by now (6 ticks), so swing again on the strike
			mob.swing(InteractionHand.MAIN_HAND);
			trace(target, name(mob) + " strikes");
			releasing = true;
			try {
				mob.doHurtTarget(level, target);
			} finally {
				releasing = false;
			}
		}
	}

	/**
	 * A block staggers the melee attacker; a perfect parry also pushes it back. LOWEST, so it sees the final
	 * blocked state and the perfect stamp WitchercraftSwordBlock.onBlock wrote at HIGH. The strike's PENDING
	 * entry is gone by now, so the bracing below doesn't swallow this push.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void staggerOnBlock(LivingShieldBlockEvent event) {
		if (!event.getBlocked() || event.getBlockedDamage() <= 0 || !(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level) || !WitchercraftSwordBlock.hasCounterattack(player))
			return;
		if (!(event.getDamageSource().getEntity() instanceof Mob mob) || event.getDamageSource().getDirectEntity() != mob)
			return;
		boolean perfect = player.getPersistentData().getDoubleOr(WitchercraftSwordBlock.PERFECT_PARRY_TICK, 0) == player.tickCount;
		STAGGERED.put(mob, level.getGameTime() + (perfect ? PARRY_STAGGER_TICKS : BLOCK_STAGGER_TICKS));
		if (perfect)
			mob.knockback(PARRY_KNOCKBACK, player.getX() - mob.getX(), player.getZ() - mob.getZ());
		trace(player, name(mob) + " staggered " + (perfect ? PARRY_STAGGER_TICKS : BLOCK_STAGGER_TICKS) + " ticks");
	}

	/** A mob winding up is braced: hitting it trades blows instead of knocking it out of reach. */
	@SubscribeEvent
	public static void braceDuringWindup(LivingKnockBackEvent event) {
		if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide() && PENDING.containsKey(mob))
			event.setCanceled(true);
	}

	/**
	 * DevLog hit trace for the player being hit (not the attacker): every wind-up, strike, block and hit that
	 * lands on you, stamped with your tickCount, so attack rhythm can be read in numbers. LOWEST and not
	 * receiving cancelled events, so only what really reaches you is printed.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void traceIncoming(LivingIncomingDamageEvent event) {
		Entity src = event.getSource().getEntity();
		trace(event.getEntity(), "hit by " + (src != null ? name(src) : event.getSource().getMsgId()) + " for " + Math.round(event.getAmount() * 100) / 100.0);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void traceBlock(LivingShieldBlockEvent event) {
		if (!event.getBlocked())
			return;
		boolean perfect = event.getEntity().getPersistentData().getDoubleOr(WitchercraftSwordBlock.PERFECT_PARRY_TICK, 0) == event.getEntity().tickCount;
		trace(event.getEntity(), (perfect ? "PERFECT PARRY " : "blocked ") + Math.round(event.getBlockedDamage() * 100) / 100.0 + " of " + Math.round(event.getDamageContainer().getNewDamage() * 100) / 100.0);
	}

	static void trace(Entity whom, String text) {
		if (whom instanceof ServerPlayer player && player.hasEffect(WitchercraftModMobEffects.DEV_LOG))
			player.sendSystemMessage(Component.literal("\u00A77[" + player.tickCount + "] " + text), false);
	}

	private static String name(Entity entity) {
		return entity.getName().getString() + "#" + entity.getId();
	}
}
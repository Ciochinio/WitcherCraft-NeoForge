/*
 * The code of this mod element is always locked.
 *
 * HAND-MAINTAINED (locked code element WitchercraftSwordBlock, ~/Admin/Calc). Sword blocking and the
 * perfect parry (GDD 5, Blocking and parrying). Blockly cannot add a data component to vanilla items or
 * read the raise timing, so this lives in Java; the perks that react to a parry stay in Blockly and only
 * read the NBT stamp written here.
 */

package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderSet;

import java.util.Optional;
import java.util.List;

@EventBusSubscriber
public final class WitchercraftSwordBlock {
	/** Tick of the last perfect parry, on the blocker. Counterattack (WitchercraftOnShieldBlock) compares it with tickCount. */
	public static final String PERFECT_PARRY_TICK = "witchercraftPerfectParryTick";
	private static final String RAISE_TICK = "witchercraftParryRaiseTick";
	private static final String WINDOW_START = "witchercraftParryWindowStart";

	/** A hit this many ticks after the block becomes active (or sooner) is a perfect parry. */
	public static final int PERFECT_WINDOW_TICKS = 5;
	/** A raise opens a perfect window only if the previous raise started at least this long ago; stops right-click spam. */
	public static final int RERAISE_LOCKOUT_TICKS = 20;
	public static final float SWORD_BLOCK_FACTOR = 0.5f;

	private WitchercraftSwordBlock() {
	}

	/** Melee payoff: perfect parry of melee hits, stagger, the incoming-attack indicator. */
	public static boolean hasCounterattack(Player player) {
		return player.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkCounterattack;
	}

	/** Projectile payoff: any block (held or perfect) of an arrow, bolt or trident sends it back, homing on the shooter. */
	public static boolean hasArrowDeflection(Player player) {
		return player.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkArrowDeflection;
	}

	/** Either perk unlocks sword blocking and opens perfect windows; each pays off only on its own kind of hit. With neither, a shield is vanilla. */
	public static boolean canParry(Player player) {
		return hasCounterattack(player) || hasArrowDeflection(player);
	}

	/** Returned shots fly at full-bow speed; their base damage is rescaled so they deal what they would have dealt the blocker. */
	public static final float RETURN_SPEED = 3.0f;
	/** While blocking with Arrow Deflection, a shot crossing your hitbox grown by this much on every side (front half only) is caught. */
	public static final double CATCH_MARGIN = 1.0;
	/** Share of the way toward the shooter a returned shot turns each tick, and for how long it keeps homing. */
	public static final double HOMING_STEER = 0.3;
	/**
	 * A projectile is returned only if it reaches you this soon after a fresh raise (same window start and re-raise
	 * lockout as the perfect parry). Longer than the melee window: an arrow flies 0.35 s from 10 blocks, so the cue is
	 * the shoot sound, not the arrow. A block held past it is a plain block.
	 */
	public static final int PROJECTILE_WINDOW_TICKS = 10;

	private static boolean inProjectileWindow(Player player) {
		double start = player.getPersistentData().getDoubleOr(WINDOW_START, 0);
		int now = player.tickCount;
		return start > 0 && now - start >= 0 && now - start <= PROJECTILE_WINDOW_TICKS;
	}
	public static final int HOMING_TICKS = 60;

	private record Deflect(AbstractArrow arrow, Player player, @Nullable Entity shooter, float damage) {
	}

	private record Homing(AbstractArrow shot, LivingEntity target, long until) {
	}

	/** Server thread only. Vanilla reverses a blocked arrow at 20% speed right after the block, so that return shot is made at the end of the tick. */
	private static final List<Deflect> DEFLECTS = new ArrayList<>();
	private static final List<Homing> HOMING = new ArrayList<>();
	private static java.lang.reflect.Field baseDamageField;

	private static BlocksAttacks swordBlock(Level level) {
		HolderSet<DamageType> bypass = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.BYPASSES_SHIELD);
		return new BlocksAttacks(0f, 1f, List.of(new BlocksAttacks.DamageReduction(90f, Optional.empty(), 0f, SWORD_BLOCK_FACTOR)),
				new BlocksAttacks.ItemDamageFunction(0f, 1f, 0f), Optional.of(bypass), Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK));
	}

	/**
	 * Every blocking item (vanilla shield, modded shields) blocks on the press, like the sword, so one perfect
	 * window and one indicator timing fit all. Default components, so it can't follow a world option. LOWEST so
	 * other mods' component edits are already in.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void noRaiseDelay(ModifyDefaultComponentsEvent event) {
		event.modifyMatching((item, components) -> {
			BlocksAttacks blocks = components.get(DataComponents.BLOCKS_ATTACKS);
			return blocks != null && blocks.blockDelaySeconds() > 0;
		}, (builder, context, item) -> {
			BlocksAttacks b = builder.get(DataComponents.BLOCKS_ATTACKS);
			if (b != null)
				builder.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0f, b.disableCooldownScale(), b.damageReductions(), b.itemDamage(), b.bypassedBy(), b.blockSound(), b.disableSound()));
		});
	}

	/**
	 * The component is put on the sword stack when it is right-clicked with an empty off-hand, and taken off
	 * again when it is right-clicked while blocking is not allowed, so the off-hand item (shield, chain hook,
	 * bomb, torch) is used instead. This runs on both sides before Item.use, which is what starts the block.
	 * Swords that block by default (another mod's) are never touched.
	 */
	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		ItemStack stack = event.getItemStack();
		if (!stack.is(ItemTags.SWORDS) || stack.getPrototype().has(DataComponents.BLOCKS_ATTACKS))
			return;
		Player player = event.getEntity();
		boolean allowed = WorldMapServerConfig.swordBlocking() && player.getOffhandItem().isEmpty() && canParry(player);
		if (allowed && !stack.has(DataComponents.BLOCKS_ATTACKS))
			stack.set(DataComponents.BLOCKS_ATTACKS, swordBlock(player.level()));
		else if (!allowed && stack.has(DataComponents.BLOCKS_ATTACKS))
			stack.remove(DataComponents.BLOCKS_ATTACKS);
	}

	/** Any block (sword or shield): open a perfect window when the raise is fresh. Server only. */
	@SubscribeEvent
	public static void onUseStart(LivingEntityUseItemEvent.Start event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide())
			return;
		BlocksAttacks blocks = event.getItem().get(DataComponents.BLOCKS_ATTACKS);
		if (blocks == null)
			return;
		CompoundTag data = player.getPersistentData();
		if (!canParry(player)) {
			data.putDouble(WINDOW_START, 0);
			return;
		}
		int now = player.tickCount;
		double last = data.getDoubleOr(RAISE_TICK, 0);
		// tickCount restarts on relog and respawn, so a stamp from before reads as negative and counts as fresh
		boolean locked = last > 0 && now - last >= 0 && now - last < RERAISE_LOCKOUT_TICKS;
		data.putDouble(WINDOW_START, locked ? 0 : Math.max(1, now + blocks.blockDelayTicks()));
		data.putDouble(RAISE_TICK, Math.max(1, now));
	}

	/**
	 * Runs before the Blockly shield-block procedure (HIGH priority), so the parry stamp is set when it reads it.
	 * Melee: only a hit inside the perfect window, with Counterattack. Projectiles: an arrow, bolt or trident blocked
	 * inside the projectile window, with Arrow Deflection (most are caught earlier by onServerTickPre; this is for the
	 * ones that get through).
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlock(LivingShieldBlockEvent event) {
		if (!(event.getEntity() instanceof Player player) || !event.getBlocked() || event.getOriginalBlockedDamage() <= 0)
			return;
		Entity direct = event.getDamageSource().getDirectEntity();
		if (direct instanceof Projectile) {
			if (!(direct instanceof AbstractArrow arrow) || !hasArrowDeflection(player) || !inProjectileWindow(player))
				return;
			event.setBlockedDamage(event.getDamageContainer().getNewDamage());
			if (player.level() instanceof ServerLevel level) {
				parryFeedback(level, player);
				DEFLECTS.add(new Deflect(arrow, player, event.getDamageSource().getEntity(), event.getDamageContainer().getOriginalDamage()));
			}
			return;
		}
		CompoundTag data = player.getPersistentData();
		int now = player.tickCount;
		double start = data.getDoubleOr(WINDOW_START, 0);
		if (!(start > 0 && now - start >= 0 && now - start <= PERFECT_WINDOW_TICKS) || !hasCounterattack(player))
			return;
		event.setBlockedDamage(event.getDamageContainer().getNewDamage());
		data.putDouble(PERFECT_PARRY_TICK, now);
		if (player.level() instanceof ServerLevel level)
			parryFeedback(level, player);
	}

	private static void parryFeedback(ServerLevel level, Player player) {
		Vec3 at = player.getEyePosition().add(player.getLookAngle().scale(0.7)).subtract(0, 0.3, 0);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.45f, 1.9f);
		level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 12, 0.15, 0.15, 0.15, 0.25);
	}

	/**
	 * Before anything moves this tick: steer returned shots, then catch any shot whose path this tick crosses
	 * the catch zone of a player blocking with Arrow Deflection. A caught shot never reaches the hurt code.
	 */
	@SubscribeEvent
	public static void onServerTickPre(ServerTickEvent.Pre event) {
		steerHoming();
		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			if (!player.isAlive() || !player.isBlocking() || !hasArrowDeflection(player) || !inProjectileWindow(player) || !(player.level() instanceof ServerLevel level))
				continue;
			AABB zone = player.getBoundingBox().inflate(CATCH_MARGIN);
			Vec3 look = player.getLookAngle().multiply(1, 0, 1);
			for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, zone.inflate(4.0))) {
				Vec3 v = arrow.getDeltaMovement();
				// owner == player covers our own returned shots; pierce mirrors vanilla, where piercing arrows can't be blocked
				if (arrow.isRemoved() || arrow.getOwner() == player || arrow.getPierceLevel() > 0 || v.lengthSqr() < 0.01)
					continue;
				Vec3 from = arrow.position();
				Vec3 toPlayer = player.position().subtract(from);
				if (v.dot(toPlayer) <= 0 || look.dot(toPlayer.multiply(-1, 0, -1)) <= 0)
					continue; // moving away, or coming from behind the block
				if (!zone.contains(from) && zone.clip(from, from.add(v)).isEmpty())
					continue;
				parryFeedback(level, player);
				WitchercraftAttackWindup.trace(player, "caught " + arrow.getName().getString() + "#" + arrow.getId());
				launchBack(level, arrow, player, arrow.getOwner(), wouldDeal(arrow));
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (DEFLECTS.isEmpty())
			return;
		List<Deflect> due = new ArrayList<>(DEFLECTS);
		DEFLECTS.clear();
		for (Deflect d : due) {
			if (d.player().isAlive() && d.player().level() instanceof ServerLevel level && d.arrow().level() == level) {
				WitchercraftAttackWindup.trace(d.player(), "blocked " + d.arrow().getName().getString() + "#" + d.arrow().getId() + (d.arrow().isRemoved() ? " (already gone, not returned)" : ""));
				if (!d.arrow().isRemoved())
					launchBack(level, d.arrow(), d.player(), d.shooter(), d.damage());
			}
		}
	}

	private static void launchBack(ServerLevel level, AbstractArrow arrow, Player player, @Nullable Entity shooter, float damage) {
		LivingEntity target = shooter instanceof LivingEntity living && living != player && living.isAlive() && living.level() == level && living.distanceToSqr(player) < 64 * 64 ? living : null;
		Vec3 eye = player.getEyePosition().subtract(0, 0.1, 0);
		Vec3 dir = target != null ? centre(target).subtract(eye) : player.getLookAngle();
		Vec3 from = eye.add(dir.normalize().scale(0.6));
		AbstractArrow shot;
		if (arrow instanceof ThrownTrident) {
			// a trident deals damage only once (private dealtDamage), so a fresh one is thrown; no enchantments, so loyalty can't bring it to you
			ItemStack item = arrow.getPickupItemStackOrigin().copy();
			item.remove(DataComponents.ENCHANTMENTS);
			arrow.discard();
			shot = new ThrownTrident(level, from.x, from.y, from.z, item);
			level.addFreshEntity(shot);
		} else {
			arrow.setPos(from);
			arrow.setBaseDamage(damage / RETURN_SPEED);
			shot = arrow;
		}
		shot.setOwner(player);
		shot.pickup = AbstractArrow.Pickup.DISALLOWED; // after setOwner, which flips it to ALLOWED for a player owner
		shot.shoot(dir.x, dir.y, dir.z, RETURN_SPEED, 0f);
		if (target != null)
			HOMING.add(new Homing(shot, target, level.getGameTime() + HOMING_TICKS));
		level.playSound(null, from.x, from.y, from.z, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1f, 1.2f);
		WitchercraftAttackWindup.trace(player, "returned to " + (target != null ? target.getName().getString() + "#" + target.getId() : "crosshair") + " for " + Math.round(damage * 100) / 100.0);
	}

	/** Re-shot at full speed each tick, so drag and drop never eat into it and the damage stays what was blocked. */
	private static void steerHoming() {
		Iterator<Homing> it = HOMING.iterator();
		while (it.hasNext()) {
			Homing h = it.next();
			AbstractArrow shot = h.shot();
			Vec3 v = shot.getDeltaMovement();
			if (shot.isRemoved() || !h.target().isAlive() || shot.level() != h.target().level() || shot.level().getGameTime() > h.until() || v.lengthSqr() < 0.01) {
				it.remove();
				continue;
			}
			Vec3 want = centre(h.target()).subtract(shot.position()).normalize();
			Vec3 dir = v.normalize().scale(1 - HOMING_STEER).add(want.scale(HOMING_STEER));
			shot.shoot(dir.x, dir.y, dir.z, RETURN_SPEED, 0f);
		}
	}

	private static Vec3 centre(LivingEntity target) {
		return new Vec3(target.getX(), target.getY(0.5), target.getZ());
	}

	/** What the shot would deal on hitting now (arrow damage is ceil(speed x base)). The base has no getter; a trident's is unused (it deals a fixed 8). */
	private static float wouldDeal(AbstractArrow arrow) {
		double base = 2.0;
		try {
			if (baseDamageField == null) {
				baseDamageField = AbstractArrow.class.getDeclaredField("baseDamage");
				baseDamageField.setAccessible(true);
			}
			base = baseDamageField.getDouble(arrow);
		} catch (ReflectiveOperationException | RuntimeException e) {
			// keep the vanilla default
		}
		return (float) Math.ceil(arrow.getDeltaMovement().length() * base);
	}
}
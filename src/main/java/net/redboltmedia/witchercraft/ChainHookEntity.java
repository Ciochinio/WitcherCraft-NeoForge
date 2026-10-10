package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModItems;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.EventHooks;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.core.registries.Registries;

import java.util.WeakHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Chain Hook projectile: a hook thrown from a charged spin that stays attached to the
 * living entity it hits until the owner reels it in with a second right-click.
 *
 * Charging: the item is held like a bow. {@link #chargeTick} plays the spin sounds and
 * {@link #release} throws at the stage reached (STAGE_2_TICKS, STAGE_3_TICKS), which
 * picks the range and launch speed from STAGE_RANGE and STAGE_SPEED.
 *
 * Lifecycle (server authoritative):
 *   FLYING  - flight from the owner's eyes. A block hit, leaving the stage range, or a
 *             cancel click retracts it as a miss (MISS_COOLDOWN).
 *   HOOKED  - attached to a target; deals HOOK_DAMAGE and costs 1 durability on
 *             contact. Breaks (HIT_COOLDOWN) when the target dies or changes
 *             dimension, the chain stretches past BREAK_RANGE, or the owner idles
 *             for HOOKED_TIMEOUT ticks.
 *   PULLING - started by the next right-click: a PULL_LIFT hop, then for PULL_TICKS the
 *             target's horizontal velocity is set toward the owner (see {@link #tickPull}),
 *             sized to land it STOP_DISTANCE away and scaled down by knockback
 *             resistance. Ends with HIT_COOLDOWN.
 * Switching away from the item retracts the hook in any state.
 *
 * The hooked target's entity id is synced so the client can pin the hook to it and
 * ChainHookRenderer can draw the chain. Each player's live hook is tracked in one weak
 * map per side, so the client can also tell that a right-click will reel rather than
 * charge. ChainHookUse, ChainHookChargeTick and ChainHookRelease call in here.
 *
 * HAND-MAINTAINED: locked code element. The EntityType is registered here, not
 * through an MCreator element; {@link #ENTITY_TYPES} is attached to the mod bus in
 * WitchercraftMod's "mod init" user code block.
 */
public class ChainHookEntity extends Projectile {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, WitchercraftMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<ChainHookEntity>> TYPE = ENTITY_TYPES.register("chain_hook",
			() -> EntityType.Builder.<ChainHookEntity>of(ChainHookEntity::new, MobCategory.MISC).noLootTable().noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(2)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "chain_hook"))));

	// Tuning. Keep in sync with the Chain Hook section of GAME_DESIGN_DOCUMENT.md.
	/** Use ticks at which the spin reaches stage 2 and stage 3. */
	public static final int STAGE_2_TICKS = 10;
	public static final int STAGE_3_TICKS = 20;
	/** Per stage (index 0 = stage 1): maximum flight range in blocks and launch speed in blocks per tick. */
	public static final double[] STAGE_RANGE = {8.0, 12.0, 16.0};
	public static final float[] STAGE_SPEED = {1.0F, 1.4F, 1.8F};
	/** Ticks between spin whooshes per stage. */
	public static final int[] STAGE_WHOOSH_INTERVAL = {10, 6, 3};
	public static final double BREAK_RANGE = 20.0;
	public static final double GRAVITY = 0.02;
	public static final double AIR_DRAG = 0.99;
	public static final float HOOK_DAMAGE = 3.0F;
	public static final double STOP_DISTANCE = 3.0;
	/** Pull: length in ticks, horizontal speed cap in blocks per tick, and the one-off upward hop at the start. */
	public static final int PULL_TICKS = 8;
	public static final double MAX_PULL_SPEED = 2.0;
	public static final double PULL_LIFT = 0.3;
	/** Fraction of its horizontal speed the target keeps when the pull ends; without braking it coasts past the owner. */
	public static final double PULL_END_CARRY = 0.3;
	/** Vertical pull when the owner is higher: at most MAX_PULL_RISE_SPEED blocks per tick, and never more than MAX_RISE blocks above where the pull started. */
	public static final double MAX_PULL_RISE_SPEED = 0.6;
	public static final double MAX_RISE = 5.0;
	public static final int HOOKED_TIMEOUT = 200;
	public static final int MISS_COOLDOWN = 20;
	public static final int HIT_COOLDOWN = 60;

	private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(ChainHookEntity.class, EntityDataSerializers.INT);
	private static final Map<Player, ChainHookEntity> SERVER_ACTIVE = new WeakHashMap<>();
	private static final Map<Player, ChainHookEntity> CLIENT_ACTIVE = new WeakHashMap<>();

	private final InterpolationHandler interpolationHandler = new InterpolationHandler(this);
	private @Nullable Entity hookedIn;
	private double maxRange = STAGE_RANGE[STAGE_RANGE.length - 1];
	private int hookedTicks;
	private int pullTicksLeft = -1;
	private double pullStartY;

	public ChainHookEntity(EntityType<? extends ChainHookEntity> type, Level level) {
		super(type, level);
	}

	/** Spin stage (1-3) for a number of ticks spent charging. */
	public static int stageFor(int ticksUsing) {
		return ticksUsing >= STAGE_3_TICKS ? 3 : ticksUsing >= STAGE_2_TICKS ? 2 : 1;
	}

	/**
	 * Right-click, both sides. The item has already started charging; with a hook out the
	 * click instead stops the charge and, on the server, cancels or reels the hook.
	 */
	public static void use(Player player, InteractionHand hand) {
		ChainHookEntity hook = activeHook(player);
		if (hook == null)
			return;
		player.stopUsingItem();
		if (player.level().isClientSide())
			return;
		if (hook.hookedIn == null) {
			hook.retract(false);
		} else if (hook.pullTicksLeft < 0) {
			hook.startPull(hook.hookedIn);
		}
		player.swing(hand, true);
	}

	/** Every tick while charging, server side: spin whooshes and a click on each new stage. */
	public static void chargeTick(Player player) {
		if (player.level().isClientSide() || activeHook(player) != null)
			return;
		int ticks = player.getTicksUsingItem();
		int stage = stageFor(ticks);
		if (ticks == STAGE_2_TICKS || ticks == STAGE_3_TICKS)
			playAt(player, (ticks == STAGE_3_TICKS ? SoundEvents.CROSSBOW_LOADING_END : SoundEvents.CROSSBOW_LOADING_MIDDLE).value(), 0.8F, 1.0F);
		if (ticks % STAGE_WHOOSH_INTERVAL[stage - 1] == 0)
			playAt(player, SoundEvents.PLAYER_ATTACK_NODAMAGE, 0.5F, 0.7F + 0.25F * stage);
	}

	/** Releasing the charge, server side: throws the hook at the stage reached. */
	public static void release(Player player, InteractionHand hand) {
		if (!(player.level() instanceof ServerLevel level) || activeHook(player) != null)
			return;
		int stage = stageFor(player.getTicksUsingItem());
		ChainHookEntity hook = new ChainHookEntity(TYPE.get(), level);
		hook.setOwner(player);
		hook.maxRange = STAGE_RANGE[stage - 1];
		hook.snapTo(player.getX(), player.getEyeY() - 0.1, player.getZ(), player.getYRot(), player.getXRot());
		Projectile.spawnProjectile(hook, level, player.getItemInHand(hand), h -> h.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, STAGE_SPEED[stage - 1], 0.5F));
		SERVER_ACTIVE.put(player, hook);
		playAt(player, SoundEvents.FISHING_BOBBER_THROW, 0.6F, 0.6F + 0.15F * stage);
		player.swing(hand, true);
	}

	public static @Nullable ChainHookEntity activeHook(Player player) {
		ChainHookEntity hook = (player.level().isClientSide() ? CLIENT_ACTIVE : SERVER_ACTIVE).get(player);
		return hook == null || hook.isRemoved() ? null : hook;
	}

	@Override
	public InterpolationHandler getInterpolation() {
		return this.interpolationHandler;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_HOOKED_ENTITY, 0);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		if (DATA_HOOKED_ENTITY.equals(accessor)) {
			int id = this.getEntityData().get(DATA_HOOKED_ENTITY);
			this.hookedIn = id > 0 ? this.level().getEntity(id - 1) : null;
		}
		super.onSyncedDataUpdated(accessor);
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket packet) {
		super.recreateFromPacket(packet);
		Player owner = this.getPlayerOwner();
		if (owner != null)
			CLIENT_ACTIVE.put(owner, this);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 4096.0;
	}

	@Override
	public void tick() {
		this.getInterpolation().interpolate();
		super.tick();
		Player owner = this.getPlayerOwner();
		if (owner == null || !owner.isAlive() || owner.level() != this.level()) {
			this.discard();
			return;
		}
		boolean server = !this.level().isClientSide();
		if (server && findHand(owner) == null) {
			this.retract(this.hookedIn != null);
			return;
		}
		if (this.hookedIn != null) {
			this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.6), this.hookedIn.getZ());
			this.setDeltaMovement(Vec3.ZERO);
			if (server)
				this.tickHooked(owner, this.hookedIn);
			return;
		}
		if (server) {
			if (this.distanceToSqr(owner) > this.maxRange * this.maxRange) {
				this.retract(false);
				return;
			}
			HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			if (hit.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hit))
				this.onHit(hit);
			if (this.isRemoved() || this.hookedIn != null)
				return;
		}
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.updateRotation();
		this.setDeltaMovement(this.getDeltaMovement().scale(AIR_DRAG).add(0.0, -GRAVITY, 0.0));
		this.reapplyPosition();
	}

	private void tickHooked(Player owner, Entity target) {
		if (!target.isAlive() || target.level() != this.level()) {
			this.retract(true);
			return;
		}
		if (this.pullTicksLeft >= 0) {
			this.tickPull(owner, target);
			return;
		}
		if (this.distanceToSqr(owner) > BREAK_RANGE * BREAK_RANGE || ++this.hookedTicks > HOOKED_TIMEOUT)
			this.retract(true);
	}

	private void startPull(Entity target) {
		this.pullTicksLeft = PULL_TICKS;
		this.pullStartY = target.getY();
		Vec3 motion = target.getDeltaMovement();
		target.setDeltaMovement(motion.x, Math.max(motion.y, 0.0) + PULL_LIFT * this.pullFactor(target), motion.z);
		target.hurtMarked = true;
		this.playOwnerSound(SoundEvents.CHAIN_BREAK, 1.0F);
	}

	/**
	 * One pull tick: horizontal velocity toward the owner, sized so the target arrives
	 * STOP_DISTANCE away on the last of PULL_TICKS ticks, capped at MAX_PULL_SPEED.
	 * When the owner is higher, the target also gets the upward speed that reaches the
	 * owner's height by the last tick (plus one tick of gravity), capped at
	 * MAX_PULL_RISE_SPEED and stopped MAX_RISE above the start. That speed REPLACES the
	 * vertical velocity when larger and is never added to it: the first version added the
	 * vertical part of the pull every tick, which compounded and launched mobs skyward.
	 */
	private void tickPull(Player owner, Entity target) {
		Vec3 toOwner = owner.position().subtract(target.position());
		double horizontal = toOwner.horizontalDistance();
		if (this.pullTicksLeft == 0 || horizontal <= STOP_DISTANCE) {
			// Brake: airborne mobs lose only ~9% speed per tick and would coast far past the owner.
			Vec3 motion = target.getDeltaMovement();
			target.setDeltaMovement(motion.x * PULL_END_CARRY, Math.min(motion.y, 0.0), motion.z * PULL_END_CARRY);
			target.hurtMarked = true;
			this.retract(true);
			return;
		}
		double factor = this.pullFactor(target);
		double speed = Math.min((horizontal - STOP_DISTANCE) / this.pullTicksLeft, MAX_PULL_SPEED) * factor;
		double vy = target.getDeltaMovement().y;
		if (toOwner.y > 0.0 && target.getY() < this.pullStartY + MAX_RISE)
			vy = Math.max(vy, Math.min(toOwner.y / this.pullTicksLeft + 0.08, MAX_PULL_RISE_SPEED) * factor);
		target.setDeltaMovement(toOwner.x / horizontal * speed, vy, toOwner.z / horizontal * speed);
		target.hurtMarked = true;
		this.pullTicksLeft--;
	}

	private double pullFactor(Entity target) {
		if (target instanceof LivingEntity living)
			return Math.max(0.0, 1.0 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
		return 1.0;
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator() && entity != this.getOwner();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!(this.level() instanceof ServerLevel level) || !(this.getOwner() instanceof Player owner))
			return;
		Entity target = result.getEntity();
		boolean damaged = target.hurtServer(level, this.damageSources().thrown(this, owner), HOOK_DAMAGE);
		// A player who could not be damaged (PvP off, creative) is not hooked either.
		if (!damaged && target instanceof Player) {
			this.retract(false);
			return;
		}
		if (!target.isAlive()) {
			this.retract(true);
			return;
		}
		this.setHookedEntity(target);
		this.playOwnerSound(SoundEvents.CHAIN_HIT, 1.0F);
		InteractionHand hand = findHand(owner);
		if (hand != null)
			owner.getItemInHand(hand).hurtAndBreak(1, owner, hand.asEquipmentSlot());
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		this.level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z, SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 0.8F, 1.0F);
		this.retract(false);
	}

	private void setHookedEntity(@Nullable Entity entity) {
		this.hookedIn = entity;
		this.getEntityData().set(DATA_HOOKED_ENTITY, entity == null ? 0 : entity.getId() + 1);
	}

	/** Ends the hook and puts the item on cooldown: HIT_COOLDOWN after a hook landed, MISS_COOLDOWN otherwise. */
	private void retract(boolean landed) {
		if (this.isRemoved())
			return;
		Player owner = this.getPlayerOwner();
		if (owner != null) {
			owner.getCooldowns().addCooldown(new ItemStack(WitchercraftModItems.CHAIN_HOOK.get()), landed ? HIT_COOLDOWN : MISS_COOLDOWN);
			this.playOwnerSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 0.8F);
		}
		this.discard();
	}

	private void playOwnerSound(SoundEvent sound, float volume) {
		Player owner = this.getPlayerOwner();
		if (owner != null)
			playAt(owner, sound, volume, 0.8F + this.random.nextFloat() * 0.3F);
	}

	private static void playAt(Player player, SoundEvent sound, float volume, float pitch) {
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
	}

	private static @Nullable InteractionHand findHand(Player player) {
		if (player.getMainHandItem().is(WitchercraftModItems.CHAIN_HOOK.get()))
			return InteractionHand.MAIN_HAND;
		if (player.getOffhandItem().is(WitchercraftModItems.CHAIN_HOOK.get()))
			return InteractionHand.OFF_HAND;
		return null;
	}

	public @Nullable Player getPlayerOwner() {
		return this.getOwner() instanceof Player player ? player : null;
	}

	public @Nullable Entity getHookedIn() {
		return this.hookedIn;
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		if (!this.level().isClientSide())
			forget(SERVER_ACTIVE);
		super.remove(reason);
	}

	@Override
	public void onClientRemoval() {
		forget(CLIENT_ACTIVE);
		super.onClientRemoval();
	}

	private void forget(Map<Player, ChainHookEntity> active) {
		Player owner = this.getPlayerOwner();
		if (owner != null && active.get(owner) == this)
			active.remove(owner);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean canUsePortal(boolean ignorePassenger) {
		return false;
	}
}

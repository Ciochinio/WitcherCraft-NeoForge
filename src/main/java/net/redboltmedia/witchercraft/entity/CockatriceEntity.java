package net.redboltmedia.witchercraft.entity;

import net.redboltmedia.witchercraft.procedures.CockatriceIdleAnimationConditionProcedure;
import net.redboltmedia.witchercraft.procedures.CockatriceWalkAnimationConditionProcedure;
import net.redboltmedia.witchercraft.procedures.CockatriceFlyAnimationConditionProcedure;
import net.redboltmedia.witchercraft.procedures.CockatriceHeadBashAnimationConditionProcedure;
import net.redboltmedia.witchercraft.procedures.CockatriceWingBashAnimationConditionProcedure;
import net.redboltmedia.witchercraft.init.WitchercraftModEntities;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.EnumSet;

public class CockatriceEntity extends Monster {
	/**
	 * Fly / walk tuning (20 ticks = 1 second).
	 */
	private static final int MIN_GROUND_TIME_BEFORE_FLY = 15 * 20;
	private static final int FLY_ROLL_DENOMINATOR = 80;
	private static final int MAX_FLIGHT_TIME = 6 * 20;
	private static final int LANDING_GLIDE_TICKS = 2 * 20;
	private static final double TAKEOFF_LIFT = 0.28;
	private static final double MIN_AIR_FORWARD_SPEED = 0.06;
	private static final double DESCENT_RATE = 0.055;
	private static final double NAVIGATION_FLY_SPEED = 0.55;

	private int groundTime;
	private int flightTime;
	private boolean flightWanderActive;
	private boolean descending;

	public final AnimationState animationState0 = new AnimationState();
	public final AnimationState animationState1 = new AnimationState();
	public final AnimationState animationState2 = new AnimationState();
	public final AnimationState animationState3 = new AnimationState();
	public final AnimationState animationState4 = new AnimationState();

	public CockatriceEntity(EntityType<CockatriceEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
		this.setPathfindingMalus(PathType.WATER, -1.0F);
		this.setPathfindingMalus(PathType.WATER_BORDER, -1.0F);
		this.setPathfindingMalus(PathType.FENCE, -1.0F);
		this.setGroundLocomotion();
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		GroundPathNavigation navigation = new GroundPathNavigation(this, level);
		navigation.setCanFloat(true);
		return navigation;
	}

	private void setGroundLocomotion() {
		if (this.moveControl instanceof FlyingMoveControl) {
			this.moveControl = new MoveControl(this);
		}
		if (!(this.navigation instanceof GroundPathNavigation)) {
			this.navigation.stop();
			GroundPathNavigation groundNav = new GroundPathNavigation(this, this.level());
			groundNav.setCanFloat(true);
			this.navigation = groundNav;
		}
	}

	private void setFlyingLocomotion() {
		if (!(this.moveControl instanceof FlyingMoveControl)) {
			this.moveControl = new FlyingMoveControl(this, 20, true);
		}
		if (!(this.navigation instanceof FlyingPathNavigation)) {
			this.navigation.stop();
			FlyingPathNavigation flyNav = new FlyingPathNavigation(this, this.level());
			flyNav.setCanOpenDoors(false);
			flyNav.setCanFloat(true);
			this.navigation = flyNav;
		}
	}

	private void updateLocomotion() {
		if (this.shouldUseFlyingLocomotion()) {
			this.setFlyingLocomotion();
		} else {
			this.setGroundLocomotion();
		}
	}

	private boolean shouldUseFlyingLocomotion() {
		if (this.flightWanderActive || this.descending) {
			return true;
		}
		if (!this.onGround()) {
			return true;
		}
		LivingEntity target = this.getTarget();
		return target != null && target.isAlive() && target.getY() > this.getY() + 1.5;
	}

	private boolean hasLivingTarget() {
		LivingEntity target = this.getTarget();
		return target != null && target.isAlive();
	}

	private void tryChaseTakeoff() {
		if (!this.hasLivingTarget() || !this.onGround() || this.flightWanderActive || this.descending) {
			return;
		}
		LivingEntity target = this.getTarget();
		if (target.getY() <= this.getY() + 2.0) {
			return;
		}
		double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
		if (this.distanceToSqr(target) > followRange * followRange) {
			return;
		}
		this.setFlyingLocomotion();
		this.setNoGravity(true);
		Vec3 toward = target.position().subtract(this.position()).normalize();
		this.setDeltaMovement(toward.scale(this.getFlySpeed()).add(0.0, TAKEOFF_LIFT, 0.0));
	}

	private double getGroundHeightBelow() {
		return this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
	}

	private boolean shouldGlideDown() {
		if (this.descending) {
			return true;
		}
		if (!this.flightWanderActive) {
			return false;
		}
		double groundY = this.getGroundHeightBelow();
		return this.getY() > groundY + 1.5 || this.flightTime >= MAX_FLIGHT_TIME - LANDING_GLIDE_TICKS;
	}

	private double getFlySpeed() {
		return this.getAttributeValue(Attributes.FLYING_SPEED);
	}

	private void maintainAirborneMovement() {
		Vec3 motion = this.getDeltaMovement();
		Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
		double flySpeed = this.getFlySpeed();
		boolean glidingDown = this.shouldGlideDown();
		double targetForward = glidingDown ? flySpeed * 0.35 : Math.max(flySpeed * 0.65, MIN_AIR_FORWARD_SPEED);
		double vertical = this.getDesiredVerticalSpeed(glidingDown, motion.y);

		if (this.navigation.isInProgress() && !glidingDown) {
			if (motion.horizontalDistanceSqr() < targetForward * targetForward * 0.25) {
				Vec3 push = forward.scale(targetForward);
				this.setDeltaMovement(push.x, Math.max(motion.y, 0.02), push.z);
			} else if (motion.y < 0.0) {
				this.setDeltaMovement(motion.x, motion.y * 0.5 + 0.03, motion.z);
			}
			return;
		}

		Vec3 push = forward.scale(targetForward);
		this.setDeltaMovement(push.x, vertical, push.z);
		double targetY = glidingDown ? this.getGroundHeightBelow() + 1.0 : this.getY() + 1.5;
		this.moveControl.setWantedPosition(this.getX() + forward.x * 8.0, targetY, this.getZ() + forward.z * 8.0, flySpeed * 0.7);
	}

	private double getDesiredVerticalSpeed(boolean glidingDown, double currentYMotion) {
		if (!glidingDown) {
			return Math.max(currentYMotion, 0.015);
		}
		double groundY = this.getGroundHeightBelow() + 1.0;
		double heightAbove = this.getY() - groundY;
		if (heightAbove <= 0.25) {
			return -Math.min(DESCENT_RATE * 0.5, 0.03);
		}
		double desired = (groundY - this.getY()) * 0.08;
		return Math.max(-DESCENT_RATE, Math.min(DESCENT_RATE, desired));
	}

	private void maintainGentleLanding() {
		this.maintainAirborneMovement();
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.flightWanderActive || this.descending) {
			if (this.isEffectiveAi()) {
				double flySpeed = this.getFlySpeed() * (this.descending || this.shouldGlideDown() ? 0.45 : 0.75);
				this.moveRelative((float) flySpeed, travelVector);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.92, 0.92, 0.92));
			} else {
				super.travel(travelVector);
			}
		} else {
			super.travel(travelVector);
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
			}

			@Override
			public void start() {
				CockatriceEntity.this.setFlyingLocomotion();
				super.start();
			}
		});
		this.goalSelector.addGoal(2, new CockatriceFlightWanderGoal());
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 20) {
			@Override
			public boolean canUse() {
				return !CockatriceEntity.this.hasLivingTarget() && !CockatriceEntity.this.flightWanderActive && CockatriceEntity.this.onGround() && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				return !CockatriceEntity.this.hasLivingTarget() && !CockatriceEntity.this.flightWanderActive && CockatriceEntity.this.onGround() && super.canContinueToUse();
			}
		});
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
	}

	@Override
	public boolean causeFallDamage(double distance, float damageMultiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.generic.death"));
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide()) {
			this.tryChaseTakeoff();
			if (this.flightWanderActive) {
				this.flightTime++;
				this.setNoGravity(true);
				if (this.flightTime >= MAX_FLIGHT_TIME - LANDING_GLIDE_TICKS) {
					this.descending = true;
				}
				this.maintainAirborneMovement();
				if (!this.onGround()) {
					this.groundTime = 0;
				}
			} else if (this.descending) {
				this.setNoGravity(true);
				this.maintainGentleLanding();
				if (this.onGround()) {
					this.descending = false;
					this.setNoGravity(false);
					this.flightTime = 0;
				}
			} else {
				this.setNoGravity(false);
				if (this.onGround()) {
					this.groundTime++;
					this.flightTime = 0;
				}
			}
		}
		super.tick();
		if (!this.level().isClientSide()) {
			this.updateLocomotion();
		}
		if (this.level().isClientSide()) {
			this.animationState0.animateWhen(CockatriceHeadBashAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState1.animateWhen(CockatriceWingBashAnimationConditionProcedure.execute(this), this.tickCount);
			this.animationState2.animateWhen(CockatriceFlyAnimationConditionProcedure.execute(this.getX(), this.getY(), this.getZ(), this), this.tickCount);
			this.animationState3.animateWhen(CockatriceWalkAnimationConditionProcedure.execute(this.getX(), this.getY(), this.getZ(), this), this.tickCount);
			this.animationState4.animateWhen(CockatriceIdleAnimationConditionProcedure.execute(this.getX(), this.getY(), this.getZ(), this), this.tickCount);
		}
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
		event.register(WitchercraftModEntities.COCKATRICE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(world, pos, random) && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)),
				RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 10);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
		builder = builder.add(Attributes.FLYING_SPEED, 0.9);
		return builder;
	}

	private class CockatriceFlightWanderGoal extends Goal {
		private BlockPos flightTarget;

		CockatriceFlightWanderGoal() {
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (CockatriceEntity.this.hasLivingTarget()) {
				return false;
			}
			if (CockatriceEntity.this.flightWanderActive) {
				return CockatriceEntity.this.flightTime < MAX_FLIGHT_TIME;
			}
			return CockatriceEntity.this.onGround() && CockatriceEntity.this.groundTime >= MIN_GROUND_TIME_BEFORE_FLY && CockatriceEntity.this.getRandom().nextInt(FLY_ROLL_DENOMINATOR) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return CockatriceEntity.this.flightTime < MAX_FLIGHT_TIME;
		}

		@Override
		public void start() {
			CockatriceEntity.this.flightWanderActive = true;
			CockatriceEntity.this.descending = false;
			CockatriceEntity.this.setFlyingLocomotion();
			CockatriceEntity.this.setNoGravity(true);
			Vec3 forward = Vec3.directionFromRotation(0.0F, CockatriceEntity.this.getYRot()).normalize();
			CockatriceEntity.this.setDeltaMovement(forward.scale(CockatriceEntity.this.getFlySpeed()).add(0.0, TAKEOFF_LIFT, 0.0));
			this.pickFlightTarget();
			this.moveToTarget();
		}

		@Override
		public void tick() {
			if (CockatriceEntity.this.navigation.isInProgress() && CockatriceEntity.this.navigation.getTargetPos() != null) {
				BlockPos target = CockatriceEntity.this.navigation.getTargetPos();
				if (CockatriceEntity.this.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) < 9.0) {
					this.pickFlightTarget();
					this.moveToTarget();
				}
				return;
			}
			this.pickFlightTarget();
			this.moveToTarget();
		}

		@Override
		public void stop() {
			CockatriceEntity.this.flightWanderActive = false;
			CockatriceEntity.this.navigation.stop();
			if (!CockatriceEntity.this.onGround()) {
				CockatriceEntity.this.descending = true;
			} else {
				CockatriceEntity.this.descending = false;
				CockatriceEntity.this.setNoGravity(false);
			}
		}

		private void pickFlightTarget() {
			BlockPos origin = CockatriceEntity.this.blockPosition();
			for (int attempt = 0; attempt < 12; attempt++) {
				double angle = CockatriceEntity.this.getRandom().nextDouble() * Math.PI * 2.0;
				int distance = 12 + CockatriceEntity.this.getRandom().nextInt(10);
				int dx = (int) (Math.cos(angle) * distance);
				int dz = (int) (Math.sin(angle) * distance);
				int dy = 4 + CockatriceEntity.this.getRandom().nextInt(4);
				BlockPos candidate = origin.offset(dx, dy, dz);
				if (CockatriceEntity.this.level().getBlockState(candidate).isAir() && CockatriceEntity.this.level().getBlockState(candidate.above(2)).isAir()) {
					this.flightTarget = candidate;
					return;
				}
			}
			this.flightTarget = origin.offset(CockatriceEntity.this.getRandom().nextInt(20) - 10, 5, CockatriceEntity.this.getRandom().nextInt(20) - 10);
		}

		private void moveToTarget() {
			if (this.flightTarget == null) {
				return;
			}
			CockatriceEntity.this.getLookControl().setLookAt(this.flightTarget.getX() + 0.5, this.flightTarget.getY(), this.flightTarget.getZ() + 0.5);
			CockatriceEntity.this.navigation.moveTo(this.flightTarget.getX() + 0.5, this.flightTarget.getY(), this.flightTarget.getZ() + 0.5, NAVIGATION_FLY_SPEED);
		}
	}
}

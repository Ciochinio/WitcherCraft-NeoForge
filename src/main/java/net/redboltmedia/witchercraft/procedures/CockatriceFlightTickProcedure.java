package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.entity.CockatriceEntity;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;

import java.util.WeakHashMap;
import java.util.Map;

/**
 * Adds short, directly steered flights to the otherwise MCreator-owned entity.
 * Stats and ground AI remain in the Cockatrice living-entity editor.
 */
public class CockatriceFlightTickProcedure {
	private static final int MIN_COMBAT_GROUND_TICKS = 8 * 20;
	private static final int MIN_IDLE_GROUND_TICKS = 10 * 20;
	private static final int MAX_IDLE_GROUND_TICKS = 30 * 20;
	private static final int COMBAT_FLIGHT_TICKS = 6 * 20;
	private static final int MIN_IDLE_FLIGHT_TICKS = 15 * 20;
	private static final int MAX_IDLE_FLIGHT_TICKS = 45 * 20;
	private static final int BLOCKED_TAKEOFF_RETRY_TICKS = 2 * 20;
	private static final int ATTACK_TICKS = 15;
	private static final int AIR_ATTACK_COOLDOWN = 30;
	private static final double TAKEOFF_LIFT = 0.34;
	private static final Map<CockatriceEntity, FlightMemory> MEMORY = new WeakHashMap<>();

	public static void execute(Entity entity) {
		if (!(entity instanceof CockatriceEntity cockatrice) || cockatrice.level().isClientSide())
			return;

		FlightMemory memory = MEMORY.computeIfAbsent(cockatrice, ignored -> new FlightMemory());
		tickAttackAnimation(cockatrice, memory);

		if (memory.airAttackCooldown > 0)
			memory.airAttackCooldown--;

		if (memory.flying || memory.landing) {
			tickFlight(cockatrice, memory);
		} else {
			tickGround(cockatrice, memory);
		}
	}

	private static void tickGround(CockatriceEntity cockatrice, FlightMemory memory) {
		cockatrice.setNoGravity(false);
		cockatrice.getEntityData().set(CockatriceEntity.DATA_Flying, false);

		if (!cockatrice.onGround())
			return;

		memory.groundTicks++;
		LivingEntity target = validTarget(cockatrice);
		if (target != null) {
			if (memory.groundTicks >= MIN_COMBAT_GROUND_TICKS && cockatrice.getRandom().nextInt(55) == 0 && hasTakeoffClearance(cockatrice))
				startFlight(cockatrice, memory, false);
			return;
		}

		if (memory.nextIdleTakeoffTicks == 0)
			memory.nextIdleTakeoffTicks = randomTicks(cockatrice, MIN_IDLE_GROUND_TICKS, MAX_IDLE_GROUND_TICKS);
		if (memory.groundTicks < memory.nextIdleTakeoffTicks)
			return;
		if (hasTakeoffClearance(cockatrice)) {
			startFlight(cockatrice, memory, true);
		} else {
			memory.nextIdleTakeoffTicks = memory.groundTicks + BLOCKED_TAKEOFF_RETRY_TICKS;
		}
	}

	private static void startFlight(CockatriceEntity cockatrice, FlightMemory memory, boolean idleFlight) {
		memory.flying = true;
		memory.landing = false;
		memory.flightTicks = 0;
		memory.flightLimitTicks = idleFlight ? randomTicks(cockatrice, MIN_IDLE_FLIGHT_TICKS, MAX_IDLE_FLIGHT_TICKS) : COMBAT_FLIGHT_TICKS;
		memory.groundTicks = 0;
		memory.nextIdleTakeoffTicks = 0;
		memory.changeDirectionTicks = 0;
		memory.flightOrigin = cockatrice.position();
		cockatrice.getNavigation().stop();
		cockatrice.setNoGravity(true);
		cockatrice.getEntityData().set(CockatriceEntity.DATA_Flying, true);
		Vec3 forward = Vec3.directionFromRotation(0.0F, cockatrice.getYRot()).normalize();
		cockatrice.setDeltaMovement(forward.scale(horizontalFlightSpeed(cockatrice)).add(0.0, TAKEOFF_LIFT, 0.0));
	}

	private static void tickFlight(CockatriceEntity cockatrice, FlightMemory memory) {
		cockatrice.getNavigation().stop();
		cockatrice.getEntityData().set(CockatriceEntity.DATA_Flying, true);

		if (memory.landing) {
			tickLanding(cockatrice, memory);
			return;
		}

		cockatrice.setNoGravity(true);
		memory.flightTicks++;
		if (memory.flightTicks >= memory.flightLimitTicks || cockatrice.horizontalCollision) {
			memory.landing = true;
			tickLanding(cockatrice, memory);
			return;
		}

		LivingEntity target = validTarget(cockatrice);
		Vec3 destination;
		if (target != null) {
			destination = target.position().add(0.0, 2.5, 0.0);
			tryAirAttack(cockatrice, target, memory);
		} else {
			if (memory.changeDirectionTicks-- <= 0 || memory.flightTarget == null) {
				memory.flightTarget = randomFlightTarget(cockatrice, memory.flightOrigin);
				memory.changeDirectionTicks = 25 + cockatrice.getRandom().nextInt(25);
			}
			destination = memory.flightTarget;
		}

		steerToward(cockatrice, destination);
	}

	private static void tickLanding(CockatriceEntity cockatrice, FlightMemory memory) {
		memory.flying = false;
		memory.landing = true;
		cockatrice.setNoGravity(false);
		Vec3 movement = cockatrice.getDeltaMovement();
		cockatrice.setDeltaMovement(movement.x * 0.9, Math.min(movement.y, -0.08), movement.z * 0.9);

		if (cockatrice.onGround()) {
			memory.landing = false;
			memory.flightTicks = 0;
			memory.groundTicks = 0;
			memory.nextIdleTakeoffTicks = randomTicks(cockatrice, MIN_IDLE_GROUND_TICKS, MAX_IDLE_GROUND_TICKS);
			memory.flightOrigin = null;
			memory.flightTarget = null;
			cockatrice.getEntityData().set(CockatriceEntity.DATA_Flying, false);
		}
	}

	private static void steerToward(CockatriceEntity cockatrice, Vec3 destination) {
		Vec3 offset = destination.subtract(cockatrice.position());
		if (offset.lengthSqr() < 0.01)
			return;

		double speed = horizontalFlightSpeed(cockatrice);
		Vec3 desired = offset.normalize().scale(speed);
		desired = new Vec3(desired.x, Mth.clamp(offset.y * 0.08, -0.16, 0.16), desired.z);
		Vec3 current = cockatrice.getDeltaMovement();
		Vec3 movement = current.scale(0.72).add(desired.scale(0.28));
		cockatrice.setDeltaMovement(movement);

		if (movement.horizontalDistanceSqr() > 1.0E-4) {
			float yaw = (float) (Mth.atan2(movement.z, movement.x) * 180.0 / Math.PI) - 90.0F;
			cockatrice.setYRot(Mth.rotLerp(0.25F, cockatrice.getYRot(), yaw));
			cockatrice.yBodyRot = cockatrice.getYRot();
			cockatrice.yHeadRot = cockatrice.getYRot();
		}
	}

	private static void tryAirAttack(CockatriceEntity cockatrice, LivingEntity target, FlightMemory memory) {
		if (memory.airAttackCooldown > 0 || memory.attackTicks > 0)
			return;
		if (cockatrice.distanceToSqr(target) > 20.25 || Math.abs(cockatrice.getY() - target.getY()) > 3.0)
			return;

		cockatrice.swing(InteractionHand.MAIN_HAND);
		cockatrice.getEntityData().set(CockatriceEntity.DATA_AttackAnimation, 3);
		memory.attackTicks = ATTACK_TICKS;
		memory.airAttackCooldown = AIR_ATTACK_COOLDOWN;
		if (cockatrice.level() instanceof ServerLevel serverLevel)
			cockatrice.doHurtTarget(serverLevel, target);
	}

	private static void tickAttackAnimation(CockatriceEntity cockatrice, FlightMemory memory) {
		if (memory.attackTicks > 0) {
			memory.attackTicks--;
			if (memory.attackTicks == 0)
				cockatrice.getEntityData().set(CockatriceEntity.DATA_AttackAnimation, 0);
		}

		if (!cockatrice.getEntityData().get(CockatriceEntity.DATA_Flying) && cockatrice.swinging && !memory.wasSwinging && memory.attackTicks == 0) {
			int animation = cockatrice.getRandom().nextBoolean() ? 1 : 2;
			cockatrice.getEntityData().set(CockatriceEntity.DATA_AttackAnimation, animation);
			memory.attackTicks = ATTACK_TICKS;
		}
		memory.wasSwinging = cockatrice.swinging;
	}

	private static LivingEntity validTarget(CockatriceEntity cockatrice) {
		LivingEntity target = cockatrice.getTarget();
		if (target == null || !target.isAlive())
			return null;
		double range = cockatrice.getAttributeValue(Attributes.FOLLOW_RANGE);
		return cockatrice.distanceToSqr(target) <= range * range ? target : null;
	}

	private static boolean hasTakeoffClearance(CockatriceEntity cockatrice) {
		return cockatrice.level().noCollision(cockatrice, cockatrice.getBoundingBox().expandTowards(0.0, 4.0, 0.0));
	}

	private static int randomTicks(CockatriceEntity cockatrice, int minimum, int maximum) {
		return minimum + cockatrice.getRandom().nextInt(maximum - minimum + 1);
	}

	private static Vec3 randomFlightTarget(CockatriceEntity cockatrice, Vec3 origin) {
		Level level = cockatrice.level();
		Vec3 center = origin == null ? cockatrice.position() : origin;
		double angle = cockatrice.getRandom().nextDouble() * Math.PI * 2.0;
		double distance = 8.0 + cockatrice.getRandom().nextDouble() * 8.0;
		double x = center.x + Math.cos(angle) * distance;
		double z = center.z + Math.sin(angle) * distance;
		int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
		double y = Math.max(cockatrice.getY() + 1.0, groundY + 4.0 + cockatrice.getRandom().nextInt(3));
		return new Vec3(x, y, z);
	}

	private static double horizontalFlightSpeed(CockatriceEntity cockatrice) {
		return Math.max(0.18, cockatrice.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.35);
	}

	private static final class FlightMemory {
		private int groundTicks;
		private int nextIdleTakeoffTicks;
		private int flightTicks;
		private int flightLimitTicks;
		private int changeDirectionTicks;
		private int attackTicks;
		private int airAttackCooldown;
		private boolean flying;
		private boolean landing;
		private boolean wasSwinging;
		private Vec3 flightOrigin;
		private Vec3 flightTarget;
	}
}

package net.redboltmedia.witchercraft;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Axii's aim, shared by the cast (WitchercraftAxiiCharm, server) and the aim marker (WitchercraftAxiiMarkerClient,
 * client) so the marker always shows exactly the mob a release would charm: the first mob along the look line,
 * up to RANGE, stopped by blocks. Each mob's box is widened by FORGIVENESS so the aim needn't be pixel-perfect.
 * Players and the caster's own pets are never targets.
 *
 * HAND-MAINTAINED: locked code element.
 */
public final class WitchercraftAxiiAim {
	public static final double RANGE = 16;
	public static final double FORGIVENESS = 0.3;

	private WitchercraftAxiiAim() {
	}

	public static @Nullable LivingEntity target(Player player, float partialTick) {
		Vec3 eye = player.getEyePosition(partialTick);
		Vec3 end = eye.add(player.getViewVector(partialTick).scale(RANGE));
		HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (block.getType() != HitResult.Type.MISS)
			end = block.getLocation();
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		for (Mob mob : player.level().getEntitiesOfClass(Mob.class, new AABB(eye, end).inflate(FORGIVENESS + 1), e -> e.isAlive() && !e.isSpectator())) {
			if (mob instanceof TamableAnimal pet && pet.isOwnedBy(player))
				continue;
			Optional<Vec3> hit = mob.getBoundingBox().inflate(FORGIVENESS).clip(eye, end);
			if (hit.isEmpty())
				continue;
			double d = hit.get().distanceToSqr(eye);
			if (d < bestDist) {
				bestDist = d;
				best = mob;
			}
		}
		return best;
	}
}

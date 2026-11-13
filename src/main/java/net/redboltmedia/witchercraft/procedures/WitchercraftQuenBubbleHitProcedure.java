package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.WitchercraftQuenBubbleMessage;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

// Locked: feedback for a hit Quen absorbed, called from QuenAbsorb. Sparks at the impact point (the side
// facing the attacker) for base Quen and Active Shield; for Active Shield also tells
// WitchercraftQuenBubbleClient to flash the bubble and the first-person screen edge.
public class WitchercraftQuenBubbleHitProcedure {
	/** Where the sparks appear: the bubble's surface for Active Shield (mirrors WitchercraftQuenBubbleClient), close to the body for base Quen. */
	private static final double BUBBLE_RADIUS = 1.4;
	private static final double BUBBLE_CENTRE_HEIGHT = 0.5;
	private static final double BASE_RADIUS = 0.6;
	private static final int SPARKS = 14;
	private static final double SPARK_SPREAD = 0.2;
	private static final double SPARK_SPEED = 0.6;

	public static void execute(DamageSource damagesource, Entity entity) {
		if (damagesource == null || !(entity instanceof ServerPlayer player))
			return;
		boolean active = player.hasEffect(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD);
		if (!active && !player.hasEffect(WitchercraftModMobEffects.QUEN_EFFECT))
			return;
		double dx = 0, dz = 0;
		Vec3 from = damagesource.getSourcePosition();
		if (from != null) {
			double x = from.x - player.getX(), z = from.z - player.getZ();
			double l = Math.sqrt(x * x + z * z);
			if (l > 1.0E-3) {
				dx = x / l;
				dz = z / l;
			}
		}
		double radius = active ? BUBBLE_RADIUS : BASE_RADIUS;
		double sx = dx, sz = dz;
		if (sx == 0 && sz == 0) {
			double a = player.getRandom().nextDouble() * Math.PI * 2;
			sx = Math.cos(a);
			sz = Math.sin(a);
		}
		if (player.level() instanceof ServerLevel level)
			level.sendParticles(ParticleTypes.WAX_ON, player.getX() + sx * radius, player.getY() + player.getBbHeight() * BUBBLE_CENTRE_HEIGHT, player.getZ() + sz * radius, SPARKS, SPARK_SPREAD, SPARK_SPREAD,
					SPARK_SPREAD, SPARK_SPEED);
		if (active) {
			float pool = (float) player.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield;
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new WitchercraftQuenBubbleMessage(player.getId(), pool, true, (float) dx, (float) dz));
		}
	}
}

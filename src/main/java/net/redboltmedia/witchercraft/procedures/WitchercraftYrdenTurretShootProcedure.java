package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftYrdenTrapMessage;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;

// Locked: one Magic Trap (alt Yrden) shot, called from YrdenTrapTick once a second while the turret is up.
// Shoots the nearest hostile mob the glyph can see within RANGE: indirect magic damage credited to the caster
// (WitchercraftDamageCalculator leaves INDIRECT_MAGIC alone), the YrdenZapped mark, sparks, the zap sound at the
// glyph (placeholder, user wants another), and a bolt message for
// WitchercraftYrdenTrapClient. Spends a shot only when it fires; the last shot ends the turret.
public class WitchercraftYrdenTurretShootProcedure {
	private static final double RANGE = 8;
	private static final double DAMAGE = 2;
	private static final int MARK_TICKS = 40;
	/** The bolt leaves the glyph this high above the ground (matches the drawn glyph). */
	private static final double GLYPH_HEIGHT = 0.6;

	public static void execute(Entity entity) {
		if (!(entity instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level))
			return;
		WitchercraftModVariables.PlayerVariables vars = player.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		if (vars.witchercraftYrdenShots <= 0) {
			vars.witchercraftYrdenTicks = 0;
			vars.markSyncDirty();
			return;
		}
		double gy = groundY(level, vars.witchercraftYrdenX, vars.witchercraftYrdenY, vars.witchercraftYrdenZ);
		Vec3 from = new Vec3(vars.witchercraftYrdenX, (Double.isNaN(gy) ? vars.witchercraftYrdenY : gy) + GLYPH_HEIGHT, vars.witchercraftYrdenZ);
		LivingEntity target = null;
		double best = RANGE * RANGE;
		for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, from).inflate(RANGE), e -> e instanceof Enemy && e.isAlive())) {
			Vec3 aim = mob.getBoundingBox().getCenter();
			double d = aim.distanceToSqr(from);
			if (d > best)
				continue;
			if (level.clip(new ClipContext(from, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS)
				continue;
			best = d;
			target = mob;
		}
		if (target == null)
			return;
		double intensity = player.getAttributes().hasAttribute(WitchercraftModAttributes.SIGN_INTENSITY) ? player.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).getValue() : 0;
		target.hurtServer(level, new DamageSource(level.holderOrThrow(DamageTypes.INDIRECT_MAGIC), (Entity) null, player), (float) (DAMAGE * (1 + intensity * 0.01)));
		// Like Igni: don't open the vanilla hurt cooldown, or the next sword hit inside it would be cut down.
		target.invulnerableTime = 0;
		target.addEffect(new MobEffectInstance(WitchercraftModMobEffects.YRDEN_ZAPPED, MARK_TICKS, 0, false, false));
		level.playSound(null, from.x, from.y, from.z, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1, 1.5F);
		Vec3 c = target.getBoundingBox().getCenter();
		level.sendParticles(ParticleTypes.WITCH, c.x, c.y, c.z, 12, 0.25, 0.3, 0.25, 0.1);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 6, 0.2, 0.3, 0.2, 0.3);
		vars.witchercraftYrdenShots = vars.witchercraftYrdenShots - 1;
		if (vars.witchercraftYrdenShots <= 0)
			vars.witchercraftYrdenTicks = 0;
		vars.markSyncDirty();
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
				new WitchercraftYrdenTrapMessage(player.getId(), vars.witchercraftYrdenX, vars.witchercraftYrdenY, vars.witchercraftYrdenZ, true, 1, target.getId()));
	}

	/** Same ground search as WitchercraftYrdenTrapClient: highest solid top with free space above, 4 up to 6 down. */
	private static double groundY(ServerLevel level, double x, double y, double z) {
		net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
		int bx = net.minecraft.util.Mth.floor(x), bz = net.minecraft.util.Mth.floor(z), by = net.minecraft.util.Mth.floor(y);
		for (int yy = by + 4; yy >= by - 6; yy--) {
			pos.set(bx, yy, bz);
			net.minecraft.world.phys.shapes.VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
			if (shape.isEmpty())
				continue;
			pos.set(bx, yy + 1, bz);
			if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
				continue;
			return yy + shape.max(net.minecraft.core.Direction.Axis.Y);
		}
		return Double.NaN;
	}
}

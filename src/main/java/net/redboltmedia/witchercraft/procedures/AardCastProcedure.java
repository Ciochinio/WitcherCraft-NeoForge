package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Comparator;

public class AardCastProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double toMobX = 0;
		double toMobZ = 0;
		double toMobDist = 0;
		double lookLen = 0;
		double aimDot = 0;
		double coneFactor = 0;
		double arcAngle = 0;
		double lookX = 0;
		double lookZ = 0;
		double PushForce = 0;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("AARD"), false);
		PushForce = 1.5;
		lookLen = Mth.clamp(Math.sqrt(Math.pow(entity.getLookAngle().x, 2) + Math.pow(entity.getLookAngle().z, 2)), 0.001, 100);
		lookX = entity.getLookAngle().x / lookLen;
		lookZ = entity.getLookAngle().z / lookLen;
		arcAngle = -1.0471975512;
		for (int _i1 = 0; _i1 < 40; _i1++) {
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.ENCHANTED_HIT, x, (y + 1), z, 0, (lookX * Math.cos(arcAngle) - lookZ * Math.sin(arcAngle)), (Mth.nextDouble(RandomSource.create(), 0, 0.3)), (lookX * Math.sin(arcAngle) + lookZ * Math.cos(arcAngle)),
						3);
			arcAngle = arcAngle + 0.0523598776;
		}
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (!(entityiterator instanceof Player)) {
					toMobX = entityiterator.getX() - entity.getX();
					toMobZ = entityiterator.getZ() - entity.getZ();
					toMobDist = Mth.clamp(Math.sqrt(Math.pow(toMobX, 2) + Math.pow(toMobZ, 2)), 0.5, 100);
					lookLen = Mth.clamp(Math.sqrt(Math.pow(entity.getLookAngle().x, 2) + Math.pow(entity.getLookAngle().z, 2)), 0.001, 100);
					aimDot = (toMobX / toMobDist) * (entity.getLookAngle().x / lookLen) + (toMobZ / toMobDist) * (entity.getLookAngle().z / lookLen);
					coneFactor = Mth.clamp((aimDot - 0.5) * 2, 0, 1);
					entityiterator.push(((toMobX / toMobDist) * PushForce * coneFactor), (0.45 * coneFactor), ((toMobZ / toMobDist) * PushForce * coneFactor));
				}
			}
		}
	}
}
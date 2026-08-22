package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModParticleTypes;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class QuenAuraProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double ringAngle = 0;
		ringAngle = entity.tickCount * 0.25;
		if (world instanceof ServerLevel _level)
			_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() + 0.7 * Math.cos(ringAngle)), (entity.getY() + 1 + 0.3 * Math.sin(entity.tickCount * 0.2)),
					(entity.getZ() + 0.7 * Math.sin(ringAngle)), 1, 0, 0, 0, 0);
	}
}
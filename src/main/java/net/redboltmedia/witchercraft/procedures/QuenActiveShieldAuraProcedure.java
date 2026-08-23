package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModParticleTypes;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class QuenActiveShieldAuraProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double loop = 0;
		double particleAmount = 0;
		double ringRadius = 0;
		double ringHeight = 0;
		loop = 0;
		particleAmount = 12;
		ringRadius = 1.3;
		ringHeight = 1;
		while (loop < particleAmount) {
			if (world instanceof ServerLevel _level)
				_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_HOLD_PARTICLES.get()), (entity.getX() + Math.cos(((Math.PI * 2) / particleAmount) * loop) * ringRadius),
						(entity.getY() + ringHeight), (entity.getZ() + Math.sin(((Math.PI * 2) / particleAmount) * loop) * ringRadius), 1, 0, 0, 0, 0);
			loop = loop + 1;
		}
	}
}

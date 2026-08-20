package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModParticleTypes;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleTypes;

public class YrdenCastProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("YRDEN"), false);
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.WITCH, x, y, z, 300, 2, 0.05, 2, 0.002);
		for (int _i1 = 0; _i1 < 5; _i1++) {
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x + 0), (y + 0.5), (z - 4), 0, 1, 0);
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x - 2.5), (y + 0.5), (z - 2.5), 0, 1, 0);
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x - 3), (y + 0.5), (z + 0), 0, 1, 0);
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x - 2), (y + 0.5), (z + 2.5), 0, 1, 0);
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x + 2), (y + 0.5), (z + 2.5), 0, 1, 0);
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x + 3), (y + 0.5), (z + 0), 0, 1, 0);
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.YRDEN_PARTICLE.get()), (x + 2.5), (y + 0.5), (z - 2.5), 0, 1, 0);
		}
	}
}
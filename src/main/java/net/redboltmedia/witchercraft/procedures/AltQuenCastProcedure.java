package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModParticleTypes;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.SimpleParticleType;

public class AltQuenCastProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double xRadius = 0;
		double loop = 0;
		double zRadius = 0;
		double particleAmount = 0;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("ALT QUEN"), false);
		loop = 0;
		particleAmount = 8;
		xRadius = 1.5;
		zRadius = 1.5;
		while (loop < particleAmount) {
			world.addParticle((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_HOLD_PARTICLES.get()), (x + Math.cos(((Math.PI * 2) / particleAmount) * loop) * xRadius), (y + 1),
					(z + Math.sin(((Math.PI * 2) / particleAmount) * loop) * zRadius), 0, 0.05, 0);
			loop = loop + 1;
		}
	}
}
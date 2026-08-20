package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModParticleTypes;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.SimpleParticleType;

public class QuenEffectParticlesProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.QUEN_EFFECT)) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("Message"), false);
			if (world instanceof ServerLevel _level)
				_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX()), (entity.getY() + 1), (entity.getZ() - 0.5), 1, 0.01, 0.01, 0.01, 0.01);
			WitchercraftMod.queueServerWork(5, () -> {
				if (world instanceof ServerLevel _level)
					_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() - 0.25), (entity.getY() + 1), (entity.getZ() - 0.25), 1, 0.01, 0.01, 0.01, 0.01);
				WitchercraftMod.queueServerWork(5, () -> {
					if (world instanceof ServerLevel _level)
						_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() - 0.5), (entity.getY() + 1), (entity.getZ()), 1, 0.01, 0.01, 0.01, 0.01);
					WitchercraftMod.queueServerWork(5, () -> {
						if (world instanceof ServerLevel _level)
							_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() - 0.25), (entity.getY() + 1), (entity.getZ() + 0.25), 1, 0.01, 0.01, 0.01, 0.01);
						WitchercraftMod.queueServerWork(5, () -> {
							if (world instanceof ServerLevel _level)
								_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX()), (entity.getY() + 1), (entity.getZ() + 0.5), 1, 0.01, 0.01, 0.01, 0.01);
							WitchercraftMod.queueServerWork(5, () -> {
								if (world instanceof ServerLevel _level)
									_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() + 0.25), (entity.getY() + 1), (entity.getZ() + 0.25), 1, 0.01, 0.01, 0.01, 0.01);
								WitchercraftMod.queueServerWork(5, () -> {
									if (world instanceof ServerLevel _level)
										_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() + 0.5), (entity.getY() + 1), (entity.getZ()), 1, 0.01, 0.01, 0.01, 0.01);
									WitchercraftMod.queueServerWork(5, () -> {
										if (world instanceof ServerLevel _level)
											_level.sendParticles((SimpleParticleType) (WitchercraftModParticleTypes.QUEN_PARTICLES.get()), (entity.getX() + 0.25), (entity.getY() + 1), (entity.getZ() - 0.25), 1, 0.01, 0.01, 0.01, 0.01);
										if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
											_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.QUEN_EFFECT_TIMER, 5, 1));
									});
								});
							});
						});
					});
				});
			});
		}
	}
}
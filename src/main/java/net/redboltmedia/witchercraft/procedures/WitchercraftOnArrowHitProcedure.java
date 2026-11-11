package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

public class WitchercraftOnArrowHitProcedure {
	public static void execute(Entity entity, Entity immediatesourceentity, Entity sourceentity) {
		if (entity == null || immediatesourceentity == null || sourceentity == null)
			return;
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkMaimingShot && IsCritArrowProcedure.execute(immediatesourceentity)) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.MAIMED, 80, 0));
			entity.getPersistentData().putDouble("witchercraftMaimedHits", 2);
		}
		if (sourceentity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftEquippedPerkColdBlood && IsCritArrowProcedure.execute(immediatesourceentity)
				&& immediatesourceentity.getPersistentData().getDoubleOr("witchercraftCalmShot", 0) > 0) {
			if (entity.level() instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.SNOWFLAKE, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 16, 0.3, 0.4, 0.3, 0.05);
			if (entity.level() instanceof Level _level && !_level.isClientSide())
				_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1, entity.getZ()), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.amethyst_block.chime")), SoundSource.PLAYERS, 1, (float) 1.5);
		}
	}
}

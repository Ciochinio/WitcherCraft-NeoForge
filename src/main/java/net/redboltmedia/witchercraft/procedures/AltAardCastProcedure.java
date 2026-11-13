package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Comparator;

public class AltAardCastProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity)
			_entity.swing(InteractionHand.MAIN_HAND, true);
		double dx = 0;
		double dz = 0;
		double dh = 0;
		double ang = 0;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("ALT AARD"), false);
		WitchercraftSignPlaySoundProcedure.execute(entity, "entity.player.attack.knockback", 0.7);
		for (int _i1 = 0; _i1 < 120; _i1++) {
			ang = Mth.nextDouble(RandomSource.create(), 0, 6.2831853072);
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.POOF, (x + Math.cos(ang) * 0.5), (y + Mth.nextDouble(RandomSource.create(), 0.4, 0.9)), (z + Math.sin(ang) * 0.5), 0, Math.cos(ang), (Mth.nextDouble(RandomSource.create(), -0.05, 0.08)),
						Math.sin(ang), (0.75 * Mth.nextDouble(RandomSource.create(), 0.7, 1)));
		}
		ang = 0;
		for (int _i2 = 0; _i2 < 60; _i2++) {
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.ENCHANTED_HIT, x, (y + 1), z, 0, Math.cos(ang), 0, Math.sin(ang), 3);
			ang = ang + 0.1047197551;
		}
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (!(entityiterator instanceof Player) && !(entityiterator instanceof ItemEntity) && !(entityiterator instanceof ExperienceOrb)
							&& !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt)) && WitchercraftSignCanSeeProcedure.execute(entity, entityiterator)) {
					dx = entityiterator.getX() - entity.getX();
					dz = entityiterator.getZ() - entity.getZ();
					dh = Mth.clamp(Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)), 0.5, 100);
					entityiterator.setDeltaMovement(new Vec3(((dx / dh) * 1.5), 0.4, ((dz / dh) * 1.5)));
						if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 255, false, false));
				}
			}
		}
	}
}
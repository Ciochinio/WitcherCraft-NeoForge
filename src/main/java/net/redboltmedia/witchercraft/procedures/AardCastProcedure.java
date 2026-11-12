package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
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
		if (entity instanceof LivingEntity _entity)
			_entity.swing(InteractionHand.MAIN_HAND, true);
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
		double dirX = 0;
		double dirZ = 0;
		double fanPower = 0;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("AARD"), false);
		PushForce = 1.5;
		lookLen = Mth.clamp(Math.sqrt(Math.pow((-1 * Math.sin(entity.getYRot() * 0.0174532925)), 2) + Math.pow(Math.cos(entity.getYRot() * 0.0174532925), 2)), 0.001, 100);
		lookX = (-1 * Math.sin(entity.getYRot() * 0.0174532925)) / lookLen;
		lookZ = Math.cos(entity.getYRot() * 0.0174532925) / lookLen;
		WitchercraftPlaySoundAtProcedure.execute(entity, "entity.player.attack.knockback", 0.7);
		for (int _i1 = 0; _i1 < 100; _i1++) {
			arcAngle = Mth.nextDouble(RandomSource.create(), -1.0471975512, 1.0471975512);
			dirX = lookX * Math.cos(arcAngle) - lookZ * Math.sin(arcAngle);
			dirZ = lookX * Math.sin(arcAngle) + lookZ * Math.cos(arcAngle);
			fanPower = Mth.clamp((Math.cos(arcAngle) - 0.5) * 2, 0, 1);
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.POOF, (x + dirX * 0.5), (y + Mth.nextDouble(RandomSource.create(), 0.4, 0.9)), (z + dirZ * 0.5), 0, dirX, (Mth.nextDouble(RandomSource.create(), -0.05, 0.08)), dirZ,
						((0.2 + 0.6 * fanPower) * Mth.nextDouble(RandomSource.create(), 0.7, 1)));
		}
		arcAngle = -1.0471975512;
		for (int _i2 = 0; _i2 < 41; _i2++) {
			dirX = lookX * Math.cos(arcAngle) - lookZ * Math.sin(arcAngle);
			dirZ = lookX * Math.sin(arcAngle) + lookZ * Math.cos(arcAngle);
			fanPower = Mth.clamp((Math.cos(arcAngle) - 0.5) * 2, 0, 1);
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.ENCHANTED_HIT, x, (y + 1), z, 0, dirX, (Mth.nextDouble(RandomSource.create(), 0, 0.3)), dirZ, (0.8 + 2.2 * fanPower));
			arcAngle = arcAngle + 0.0523598776;
		}
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (!(entityiterator instanceof Player) && !(entityiterator instanceof ItemEntity) && !(entityiterator instanceof ExperienceOrb)
							&& !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt)) && WitchercraftCanSeeProcedure.execute(entity, entityiterator)) {
					toMobX = entityiterator.getX() - entity.getX();
					toMobZ = entityiterator.getZ() - entity.getZ();
					toMobDist = Mth.clamp(Math.sqrt(Math.pow(toMobX, 2) + Math.pow(toMobZ, 2)), 0.5, 100);
					lookLen = Mth.clamp(Math.sqrt(Math.pow((-1 * Math.sin(entity.getYRot() * 0.0174532925)), 2) + Math.pow(Math.cos(entity.getYRot() * 0.0174532925), 2)), 0.001, 100);
					aimDot = (toMobX / toMobDist) * ((-1 * Math.sin(entity.getYRot() * 0.0174532925)) / lookLen) + (toMobZ / toMobDist) * (Math.cos(entity.getYRot() * 0.0174532925) / lookLen);
					coneFactor = Mth.clamp((aimDot - 0.5) * 2, 0, 1);
					entityiterator.push(((toMobX / toMobDist) * PushForce * coneFactor), (0.45 * coneFactor), ((toMobZ / toMobDist) * PushForce * coneFactor));
						if (coneFactor > 0) {
							if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 255, false, false));
						}
				}
			}
		}
	}
}
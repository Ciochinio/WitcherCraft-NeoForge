package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.redboltmedia.witchercraft.init.WitchercraftModEntities;
import net.redboltmedia.witchercraft.entity.IgniParticlesEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Comparator;

public class AltIgniCastProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double intensity = 0;
		double lookX = 0;
		double lookY = 0;
		double lookZ = 0;
		double beamLen = 0;
		double t = 0;
		double vX = 0;
		double vY = 0;
		double vZ = 0;
		double perp = 0;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.IGNI_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime > 20) {
			if (entity.tickCount - entity.getPersistentData().getDoubleOr("witchercraftFirestreamTick", 0) > 1) {
				WitchercraftSignPlaySoundProcedure.execute(entity, "entity.ghast.shoot", 0.8);
			}
			entity.getPersistentData().putDouble("witchercraftFirestreamTick", (entity.tickCount));
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("ALT IGNI"), false);
			intensity = 1 + (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(WitchercraftModAttributes.SIGN_INTENSITY) ? _livingEntity1.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).getValue() : 0) * 0.01;
			lookX = entity.getLookAngle().x;
			lookY = entity.getLookAngle().y;
			lookZ = entity.getLookAngle().z;
			beamLen = Mth.clamp(Math.sqrt(Math.pow(
					entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(8)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getBlockPos().getX() + 0.5 - entity.getX(), 2)
					+ Math.pow(entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(8)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getBlockPos().getY() + 0.5
							- (entity.getY() + 1.62), 2)
					+ Math.pow(entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(8)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ() + 0.5 - entity.getZ(), 2))
					- 0.5, 0.5, 8);
			{
				Entity _shootFrom = entity;
				Level projectileLevel = _shootFrom.level();
				if (!projectileLevel.isClientSide()) {
					Projectile _entityToSpawn = initArrowProjectile(new IgniParticlesEntity(WitchercraftModEntities.IGNI_PARTICLES.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 0, (byte) 5)), null, 0, true, true,
							false, AbstractArrow.Pickup.DISALLOWED);
					_entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
					_entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 4, 10);
					projectileLevel.addFreshEntity(_entityToSpawn);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime % 10 == 0) {
				final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + 1), (entity.getZ()));
				for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(18 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
					if (entityiterator instanceof LivingEntity && !(entityiterator instanceof Player)
							&& !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt)) && WitchercraftSignCanSeeProcedure.execute(entity, entityiterator)) {
						vX = entityiterator.getX() - entity.getX();
						vY = (entityiterator.getY() + entityiterator.getBbHeight() / 2) - (entity.getY() + 1.5);
						vZ = entityiterator.getZ() - entity.getZ();
						t = vX * lookX + vY * lookY + vZ * lookZ;
						perp = Math.sqrt(Mth.clamp(Math.pow(vX, 2) + Math.pow(vY, 2) + Math.pow(vZ, 2) - Math.pow(t, 2), 0, 1000));
						if (t > 0 && t <= beamLen + 0.5 && perp <= 0.6 + entityiterator.getBbWidth() / 2) {
							if (entityiterator.level() instanceof ServerLevel _serverLevel)
								entityiterator.hurtServer(_serverLevel, new DamageSource(world.holderOrThrow(DamageTypes.IN_FIRE), entity), (float) (1 * intensity));
							entityiterator.igniteForSeconds(5);
							if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.IGNI_BURN, 100, 0, false, false));
							entityiterator.getPersistentData().putDouble("witchercraftIgniBurnDps", (2 * intensity));
						}
					}
				}
			}
		}
	}

	private static AbstractArrow initArrowProjectile(AbstractArrow entityToSpawn, Entity shooter, float damage, boolean silent, boolean fire, boolean particles, AbstractArrow.Pickup pickup) {
		entityToSpawn.setOwner(shooter);
		entityToSpawn.setBaseDamage(damage);
		if (silent)
			entityToSpawn.setSilent(true);
		if (fire)
			entityToSpawn.igniteForSeconds(100);
		if (particles)
			entityToSpawn.setCritArrow(true);
		entityToSpawn.pickup = pickup;
		return entityToSpawn;
	}

	private static ItemStack createArrowWeaponItemStack(Level level, int knockback, byte piercing) {
		ItemStack weapon = new ItemStack(Items.ARROW);
		if (knockback > 0)
			weapon.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), knockback);
		if (piercing > 0)
			weapon.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING), piercing);
		return weapon;
	}
}
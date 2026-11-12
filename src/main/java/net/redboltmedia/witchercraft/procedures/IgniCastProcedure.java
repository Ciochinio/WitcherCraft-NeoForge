package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModEntities;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.entity.IgniParticlesEntity;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import java.util.Comparator;

public class IgniCastProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double intensity = 0;
		double lookX = 0;
		double lookY = 0;
		double lookZ = 0;
		double apexX = 0;
		double apexY = 0;
		double apexZ = 0;
		double vX = 0;
		double vY = 0;
		double vZ = 0;
		double dist = 0;
		for (int _i1 = 0; _i1 < 70; _i1++) {
			{
				Entity _shootFrom = entity;
				Level projectileLevel = _shootFrom.level();
				if (!projectileLevel.isClientSide()) {
					Projectile _entityToSpawn = initArrowProjectile(new IgniParticlesEntity(WitchercraftModEntities.IGNI_PARTICLES.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 0, (byte) 5)), null, 0, true, false,
							false, AbstractArrow.Pickup.DISALLOWED);
					_entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
					_entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 4, 45);
					projectileLevel.addFreshEntity(_entityToSpawn);
				}
			}
		}
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("IGNI"), false);
		WitchercraftPlaySoundAtProcedure.execute(entity, "entity.ghast.shoot", 0.8);
		intensity = 1 + (entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.SIGN_INTENSITY) ? _livingEntity0.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).getValue() : 0) * 0.01;
		lookX = entity.getLookAngle().x;
		lookY = entity.getLookAngle().y;
		lookZ = entity.getLookAngle().z;
		apexX = entity.getX() - lookX;
		apexY = (entity.getY() + 1.5) - lookY;
		apexZ = entity.getZ() - lookZ;
		{
			final Vec3 _center = new Vec3((entity.getX()), (entity.getY() + 1), (entity.getZ()));
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (entityiterator instanceof LivingEntity && !(entityiterator instanceof Player)
						&& !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt)) && WitchercraftCanSeeProcedure.execute(entity, entityiterator)) {
					vX = entityiterator.getX() - apexX;
					vY = (entityiterator.getY() + entityiterator.getBbHeight() / 2) - apexY;
					vZ = entityiterator.getZ() - apexZ;
					dist = Math.sqrt(Math.pow(vX, 2) + Math.pow(vY, 2) + Math.pow(vZ, 2));
					if (dist <= 8 && vX * lookX + vY * lookY + vZ * lookZ >= 0.819 * dist) {
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

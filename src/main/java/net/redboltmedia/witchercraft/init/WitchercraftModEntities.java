/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.entity.*;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class WitchercraftModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, WitchercraftMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<GrapeshotProjectileEntity>> GRAPESHOT_PROJECTILE = register("grapeshot_projectile",
			EntityType.Builder.<GrapeshotProjectileEntity>of(GrapeshotProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<DevilsPuffballProjectileEntity>> DEVILS_PUFFBALL_PROJECTILE = register("devils_puffball_projectile",
			EntityType.Builder.<DevilsPuffballProjectileEntity>of(DevilsPuffballProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<DancingStarProjectileEntity>> DANCING_STAR_PROJECTILE = register("dancing_star_projectile",
			EntityType.Builder.<DancingStarProjectileEntity>of(DancingStarProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<NorthernWindProjectileEntity>> NORTHERN_WIND_PROJECTILE = register("northern_wind_projectile",
			EntityType.Builder.<NorthernWindProjectileEntity>of(NorthernWindProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<SamumProjectileEntity>> SAMUM_PROJECTILE = register("samum_projectile",
			EntityType.Builder.<SamumProjectileEntity>of(SamumProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<DimeritiumBombProjectileEntity>> DIMERITIUM_BOMB_PROJECTILE = register("dimeritium_bomb_projectile",
			EntityType.Builder.<DimeritiumBombProjectileEntity>of(DimeritiumBombProjectileEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<IgniParticlesEntity>> IGNI_PARTICLES = register("igni_particles",
			EntityType.Builder.<IgniParticlesEntity>of(IgniParticlesEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<AardParticlesEntity>> AARD_PARTICLES = register("aard_particles",
			EntityType.Builder.<AardParticlesEntity>of(AardParticlesEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, registryname))));
	}
}
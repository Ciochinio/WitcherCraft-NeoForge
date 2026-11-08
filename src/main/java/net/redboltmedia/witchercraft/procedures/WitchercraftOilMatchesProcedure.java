package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class WitchercraftOilMatchesProcedure {
	public static boolean execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return false;
		return (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
				.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:necrophage_oil_enchantment")))) != 0
				&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:necrophage")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:vampire_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:vampire")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:beast_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:beast")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:insectoid_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:insectoid")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:specter_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:specter")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:ogroid_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:ogroid")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:draconid_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:draconid")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:relict_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:relict")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:cursed_oil_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:cursed")))
				|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
						.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("witchercraft:hanged_mans_venom_enchantment")))) != 0
						&& entity.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("witchercraft:humanoid")));
	}
}
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.redboltmedia.witchercraft.init;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber
public class WitchercraftModAttributes {
	public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, WitchercraftMod.MODID);
	public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE = REGISTRY.register("crit_chance", () -> new RangedAttribute("attribute.witchercraft.crit_chance", 5d, 0d, 100d).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> CRIT_DAMAGE = REGISTRY.register("crit_damage", () -> new RangedAttribute("attribute.witchercraft.crit_damage", 115d, 0d, 1000d).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> DODGE_CHANCE = REGISTRY.register("dodge_chance", () -> new RangedAttribute("attribute.witchercraft.dodge_chance", 0d, 0d, 100d).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> INSTANT_KILL_CHANCE = REGISTRY.register("instant_kill_chance", () -> new RangedAttribute("attribute.witchercraft.instant_kill_chance", 0d, 0d, 100d).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> POTION_DURATION = REGISTRY.register("potion_duration", () -> new RangedAttribute("attribute.witchercraft.potion_duration", 0d, -100d, 1000d).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> LIFE_STEAL = REGISTRY.register("life_steal", () -> new RangedAttribute("attribute.witchercraft.life_steal", 0d, 0d, 100d).setSyncable(true));
	public static final DeferredHolder<Attribute, Attribute> REFLECT_DAMAGE = REGISTRY.register("reflect_damage", () -> new RangedAttribute("attribute.witchercraft.reflect_damage", 0d, 0d, 100d).setSyncable(true));

	@SubscribeEvent
	public static void addAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, CRIT_CHANCE);
		event.add(EntityType.PLAYER, CRIT_DAMAGE);
		event.add(EntityType.PLAYER, DODGE_CHANCE);
		event.add(EntityType.PLAYER, INSTANT_KILL_CHANCE);
		event.add(EntityType.PLAYER, POTION_DURATION);
		event.add(EntityType.PLAYER, LIFE_STEAL);
		event.add(EntityType.PLAYER, REFLECT_DAMAGE);
	}
}
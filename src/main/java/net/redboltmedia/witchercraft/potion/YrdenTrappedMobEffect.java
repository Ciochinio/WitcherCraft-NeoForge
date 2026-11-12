package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class YrdenTrappedMobEffect extends MobEffect {
	public YrdenTrappedMobEffect() {
		super(MobEffectCategory.HARMFUL, -6737204);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.yrden_trapped_0"), -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class BlizzardEffectMobEffect extends MobEffect {
	public BlizzardEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
		this.addAttributeModifier(WitchercraftModAttributes.DODGE_CHANCE, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.blizzard_effect_0"), 5.0, AttributeModifier.Operation.ADD_VALUE);
		this.addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.blizzard_effect_1"), 0.55, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}
}

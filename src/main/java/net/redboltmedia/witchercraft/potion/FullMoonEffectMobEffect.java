package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class FullMoonEffectMobEffect extends MobEffect {
	public FullMoonEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
		this.addAttributeModifier(Attributes.MAX_HEALTH, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.full_moon_effect_0"), 4, AttributeModifier.Operation.ADD_VALUE);
	}
}
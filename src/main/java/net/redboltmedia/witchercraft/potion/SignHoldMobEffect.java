package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class SignHoldMobEffect extends MobEffect {
	public SignHoldMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.sign_hold_0"), -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
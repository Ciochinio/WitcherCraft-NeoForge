package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class PetrisPhilterEffectMobEffect extends MobEffect {
	public PetrisPhilterEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
		this.addAttributeModifier(WitchercraftModAttributes.SIGN_INTENSITY, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.petris_philter_effect_0"), 20.0,
				AttributeModifier.Operation.ADD_VALUE);
	}
}

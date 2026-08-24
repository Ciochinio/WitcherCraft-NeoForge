package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class SwallowEffectMobEffect extends MobEffect {
	public SwallowEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
		this.addAttributeModifier(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.swallow_effect_0"), 0.3333, AttributeModifier.Operation.ADD_VALUE);
	}
}

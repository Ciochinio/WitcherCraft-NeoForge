package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class LeshenDecoctionEffectMobEffect extends MobEffect {
	public LeshenDecoctionEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
	}
}
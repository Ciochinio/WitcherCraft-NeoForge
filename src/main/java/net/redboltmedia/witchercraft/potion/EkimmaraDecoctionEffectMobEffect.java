package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class EkimmaraDecoctionEffectMobEffect extends MobEffect {
	public EkimmaraDecoctionEffectMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
		this.addAttributeModifier(WitchercraftModAttributes.LIFE_STEAL, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.ekimmara_decoction_effect_0"), 10.0,
				AttributeModifier.Operation.ADD_VALUE);
	}
}

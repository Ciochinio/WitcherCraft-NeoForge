package net.redboltmedia.witchercraft.potion;

import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;

public class MuscleMemoryActiveMobEffect extends MobEffect {
	public MuscleMemoryActiveMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -7351297);
		this.addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "effect.muscle_memory_active_0"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}
}

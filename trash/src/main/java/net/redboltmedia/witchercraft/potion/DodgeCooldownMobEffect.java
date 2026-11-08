package net.redboltmedia.witchercraft.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class DodgeCooldownMobEffect extends MobEffect {
	public DodgeCooldownMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
	}
}
package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Igni's fire damage (the cast hit, Firestream pulses, boosted burn ticks) must not open the vanilla
 * half-second hurt cooldown: inside it a sword hit only deals what it exceeds the last hit by, so
 * "Igni + sword" would quietly lose 2-4 damage per swing. Clearing it also lets Firestream's pulses and
 * the burn ticks both land.
 */
@EventBusSubscriber
public class WitchercraftIgniHurtCooldown {
	@SubscribeEvent
	public static void afterDamage(LivingDamageEvent.Post event) {
		LivingEntity target = event.getEntity();
		DamageSource source = event.getSource();
		boolean igniHit = source.is(DamageTypes.IN_FIRE) && source.getEntity() instanceof Player;
		boolean igniBurn = source.is(DamageTypes.ON_FIRE) && target.hasEffect(WitchercraftModMobEffects.IGNI_BURN);
		if (igniHit || igniBurn)
			target.invulnerableTime = 0;
	}
}

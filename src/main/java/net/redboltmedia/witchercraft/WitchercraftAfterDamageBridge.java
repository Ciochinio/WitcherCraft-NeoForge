package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.procedures.WitchercraftAfterDamageProcedure;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Blockly has no "after damage" trigger, so this hands the damage that actually reached health
 * (after armor and absorption) to the WitchercraftAfterDamage procedure, where the logic lives.
 *
 * HAND-MAINTAINED: the locked WitchercraftAfterDamageBridge code element. If MCreator ever reorders
 * WitchercraftAfterDamage's dependencies, fix the argument order here.
 */
@EventBusSubscriber
public final class WitchercraftAfterDamageBridge {
	private WitchercraftAfterDamageBridge() {
	}

	@SubscribeEvent
	public static void afterDamage(LivingDamageEvent.Post event) {
		if (event.getEntity().level().isClientSide() || event.getHealthDamage() <= 0f)
			return;
		WitchercraftAfterDamageProcedure.execute(event.getEntity().level(), event.getSource(), event.getEntity(), event.getSource().getEntity(), event.getHealthDamage());
	}
}

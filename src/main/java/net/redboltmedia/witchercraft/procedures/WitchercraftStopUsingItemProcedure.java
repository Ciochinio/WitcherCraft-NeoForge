package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

// Locked: MCreator has no block for LivingEntity.stopUsingItem. Server-side stop syncs to the client
// through the living-entity flags, and food/potions only apply on the server, so this is enough.
public class WitchercraftStopUsingItemProcedure {
	public static void execute(Entity entity) {
		if (entity instanceof LivingEntity _living && _living.isUsingItem())
			_living.stopUsingItem();
	}
}
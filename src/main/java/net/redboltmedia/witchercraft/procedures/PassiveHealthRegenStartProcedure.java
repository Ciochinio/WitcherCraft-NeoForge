package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class PassiveHealthRegenStartProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.isAlive()) {
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth((float) (Mth
						.clamp((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + (entity instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN)
								? _livingEntity2.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).getValue()
								: 0), 0, entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)));
		}
	}
}
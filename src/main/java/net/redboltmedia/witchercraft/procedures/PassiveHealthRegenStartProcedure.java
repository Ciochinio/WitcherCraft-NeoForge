package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class PassiveHealthRegenStartProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity.isAlive()) {
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth((float) (Mth.clamp(
						(entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1)
								+ (entity instanceof LivingEntity _livEntR && _livEntR.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN)
										? _livEntR.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).getValue()
										: 0),
						0, (entity instanceof LivingEntity _livEntM ? _livEntM.getMaxHealth() : -1))));
		}
	}
}

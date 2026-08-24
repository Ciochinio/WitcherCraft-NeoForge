package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class CorrectOilEndProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && _entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) != null)
			_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).removeModifier(Identifier.parse("witchercraft:oil_correct"));
	}
}

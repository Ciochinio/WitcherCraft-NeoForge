package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class CorrectOilStartProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && _entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE) != null) {
			_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).removeModifier(Identifier.parse("witchercraft:oil_correct"));
			_entity.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).addTransientModifier(new AttributeModifier(Identifier.parse("witchercraft:oil_correct"),
					4 * (1 + (entity instanceof LivingEntity _livingEntityOD && _livingEntityOD.getAttributes().hasAttribute(WitchercraftModAttributes.OIL_DAMAGE) ? _livingEntityOD.getAttribute(WitchercraftModAttributes.OIL_DAMAGE).getValue() : 0) * 0.01), AttributeModifier.Operation.ADD_VALUE));
		}
	}
}

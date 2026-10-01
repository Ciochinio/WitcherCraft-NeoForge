package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class MutagenGreenEffectProcedure {
	public static void execute(Entity entity, double level, double synergy) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity) {
			AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_green_health"), (2 * level + 1 * synergy), AttributeModifier.Operation.ADD_VALUE);
			if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
				_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
			}
		}
	}
}

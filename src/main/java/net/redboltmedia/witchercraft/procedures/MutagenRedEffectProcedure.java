package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class MutagenRedEffectProcedure {
	public static void execute(Entity entity, double level, double synergy) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity) {
			AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_red_damage"), (10 * level + 5 * synergy), AttributeModifier.Operation.ADD_VALUE);
			if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
				_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
			}
		}
	}
}

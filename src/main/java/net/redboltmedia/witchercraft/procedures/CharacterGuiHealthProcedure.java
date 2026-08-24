package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class CharacterGuiHealthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return new java.text.DecimalFormat("##.##").format((entity instanceof LivingEntity _livingEntityMH && _livingEntityMH.getAttributes().hasAttribute(Attributes.MAX_HEALTH) ? _livingEntityMH.getAttribute(Attributes.MAX_HEALTH).getValue() : 0));
	}
}
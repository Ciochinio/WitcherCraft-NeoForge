package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class ToxicityHudOverdoseProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity instanceof LivingEntity _livingEntityTOT && _livingEntityTOT.getAttributes().hasAttribute(WitchercraftModAttributes.TOXICITY_OVERDOSE_THRESHOLD) ? _livingEntityTOT.getAttribute(WitchercraftModAttributes.TOXICITY_OVERDOSE_THRESHOLD).getValue() : 0) > 0) {
			return entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity >= (entity instanceof LivingEntity _livingEntityTOT && _livingEntityTOT.getAttributes().hasAttribute(WitchercraftModAttributes.TOXICITY_OVERDOSE_THRESHOLD) ? _livingEntityTOT.getAttribute(WitchercraftModAttributes.TOXICITY_OVERDOSE_THRESHOLD).getValue() : 0);
		}
		return false;
	}
}

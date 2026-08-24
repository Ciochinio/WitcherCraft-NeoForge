package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class GraveHagDecoctionEndProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livingEntityPHR && _livingEntityPHR.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftGraveHagDecoctionKill = 0;
				_vars.markSyncDirty();
			}
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).removeModifier(Identifier.parse("witchercraft:effect_grave_hag_kills"));
			}
		}
	}
}

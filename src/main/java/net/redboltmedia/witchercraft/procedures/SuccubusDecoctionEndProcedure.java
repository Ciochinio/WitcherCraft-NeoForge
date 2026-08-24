package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class SuccubusDecoctionEndProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livingEntityID && _livingEntityID.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftSuccubusDecoctionTick = 0;
				_vars.markSyncDirty();
			}
			if (entity instanceof LivingEntity _entity) {
				_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_succubus_stacks"));
			}
		}
	}
}

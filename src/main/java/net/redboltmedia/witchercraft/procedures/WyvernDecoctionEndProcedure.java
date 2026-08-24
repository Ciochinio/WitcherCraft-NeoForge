package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class WyvernDecoctionEndProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftWyvernDecoctionHit = 0;
			_vars.markSyncDirty();
		}
		if (entity instanceof LivingEntity _entity && _entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE) != null)
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_wyvern_hits"));
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class WyvernDecoctionTickProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)) {
			if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(WitchercraftModMobEffects.WYVERN_DECOCTION_EFFECT) ? _livEnt.getEffect(WitchercraftModMobEffects.WYVERN_DECOCTION_EFFECT).getDuration() : 0) % 20 == 0) {
				if (!(entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.IN_COMBAT))) {
					if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftWyvernDecoctionHit != 0) {
						{
							WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
							_vars.witchercraftWyvernDecoctionHit = 0;
							_vars.markSyncDirty();
						}
						if (entity instanceof LivingEntity _entity) {
							_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:effect_wyvern_hits"));
						}
					}
				}
			}
		}
	}
}
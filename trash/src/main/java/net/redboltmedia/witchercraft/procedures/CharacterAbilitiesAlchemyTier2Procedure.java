package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class CharacterAbilitiesAlchemyTier2Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.DEV_LOG) || entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerksAlchemySkillPointsUsed >= 3) {
			return true;
		}
		return false;
	}
}
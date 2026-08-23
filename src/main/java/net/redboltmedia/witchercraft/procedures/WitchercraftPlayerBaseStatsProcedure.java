package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class WitchercraftPlayerBaseStatsProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftHealth = entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1;
			_vars.witchercraftArmor = entity instanceof LivingEntity _livEnt ? _livEnt.getArmorValue() : 0;
			_vars.witchercraftBaseCritChance = 5;
			_vars.witchercraftBaseCritDamage = 115;
			_vars.witchercraftAttackSpeed = 0;
			_vars.witchercraftBasePassiveHealthRegeneration = 0;
			_vars.witchercraftBasePassiveStaminaRegeneration = 0;
			_vars.witchercraftAdditionalDamage = 0;
			_vars.witchercraftIncreasedDamage = 0;
			_vars.witchercraftToxicityOverdoseThreshold = 70;
			_vars.markSyncDirty();
		}
	}
}
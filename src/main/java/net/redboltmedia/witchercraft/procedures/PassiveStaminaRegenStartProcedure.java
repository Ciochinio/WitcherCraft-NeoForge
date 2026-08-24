package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class PassiveStaminaRegenStartProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.isAlive()) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftStaminaRegenBuffer = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftStaminaRegenBuffer
						+ (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN)
								? _livingEntity1.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).getValue()
								: 0);
				_vars.markSyncDirty();
			}
			if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) >= 20) {
				{
					WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
					_vars.witchercraftStaminaRegenBuffer = 0;
					_vars.markSyncDirty();
				}
			} else {
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftStaminaRegenBuffer >= 1) {
					if (entity instanceof Player _player)
						_player.getFoodData().setFoodLevel((int) (Mth.clamp((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) + 1, 0, 20)));
					{
						WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
						_vars.witchercraftStaminaRegenBuffer = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftStaminaRegenBuffer - 1;
						_vars.markSyncDirty();
					}
				}
			}
		}
	}
}
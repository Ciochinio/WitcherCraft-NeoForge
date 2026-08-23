package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class PassiveStaminaRegenStartProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftPassiveStaminaRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftBasePassiveStaminaRegeneration;
			_vars.markSyncDirty();
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesGourmet == true) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveStaminaRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveStaminaRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSunAndStars == true && !(world instanceof Level _lvl0 && _lvl0.isBrightOutside())) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveStaminaRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveStaminaRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.TAWNY_OWL_EFFECT)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveStaminaRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveStaminaRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.WEREWOLF_DECOCTION_EFFECT) && !(world instanceof Level _lvl3 && _lvl3.isRaining()) && !(world instanceof Level _lvl4 && _lvl4.isBrightOutside())) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveStaminaRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveStaminaRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveStaminaRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveStaminaRegeneration * 0.5;
				_vars.markSyncDirty();
			}
		}
		if (entity.isAlive()) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftStaminaRegenBuffer = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftStaminaRegenBuffer
						+ entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveStaminaRegeneration;
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

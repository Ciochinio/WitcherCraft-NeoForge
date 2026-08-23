package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class PassiveHealthRegenStartProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftPassiveHealthRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftBasePassiveHealthRegeneration;
			_vars.markSyncDirty();
		}
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.GRAVE_HAG_DECOCTION_EFFECT) && entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveHealthRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveHealthRegeneration
						+ Math.round(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftGraveHagDecoctionKill / 2) * 0.3333;
				_vars.markSyncDirty();
			}
		} else {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftGraveHagDecoctionKill = 0;
				_vars.markSyncDirty();
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSunAndStars == true && world instanceof Level _lvl2 && _lvl2.isBrightOutside()) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveHealthRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveHealthRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(WitchercraftModMobEffects.SWALLOW_EFFECT)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveHealthRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveHealthRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.TROLL_DECOCTION_EFFECT)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveHealthRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveHealthRegeneration + 0.3333;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(WitchercraftModMobEffects.IN_COMBAT)) {
			{
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftPassiveHealthRegeneration = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveHealthRegeneration * 0.5;
				_vars.markSyncDirty();
			}
		}
		if (entity.isAlive()) {
			if (entity instanceof LivingEntity _entity)
				_entity.setHealth((float) (Mth.clamp((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPassiveHealthRegeneration, 0,
						(entity instanceof LivingEntity _livEntM ? _livEntM.getMaxHealth() : -1))));
		}
	}
}

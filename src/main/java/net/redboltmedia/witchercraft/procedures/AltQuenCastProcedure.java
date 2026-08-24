package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

public class AltQuenCastProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (!(entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD))) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD, 5, 0));
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(WitchercraftModMobEffects.QUEN_EFFECT);
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield < 2) {
				WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
				_vars.witchercraftQuenShield = 2;
				_vars.markSyncDirty();
			}
		}
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.QUEN_ACTIVE_SHIELD, 5, 0));
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime % 20 == 0) {
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftQuenShield = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield
					+ 2 * (1 + (entity instanceof LivingEntity _livingEntitySI && _livingEntitySI.getAttributes().hasAttribute(WitchercraftModAttributes.SIGN_INTENSITY) ? _livingEntitySI.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).getValue() : 0) * 0.01);
			_vars.markSyncDirty();
		}
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

public class LeshenDecoctionUsedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.LESHEN_DECOCTION_EFFECT, (int) (7200 * (1
					+ (entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(WitchercraftModAttributes.POTION_DURATION) ? _livingEntity0.getAttribute(WitchercraftModAttributes.POTION_DURATION).getValue() : 0)
							* 0.01)),
					0));
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftToxicity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity + 50;
			_vars.markSyncDirty();
		}
		DecoctionUsedProcedure.execute(entity);
	}
}
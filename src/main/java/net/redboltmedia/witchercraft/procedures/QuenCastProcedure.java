package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class QuenCastProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("QUEN"), false);
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftQuenShieldMax = (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.4 * (1 + (entity instanceof LivingEntity _livingEntitySI && _livingEntitySI.getAttributes().hasAttribute(WitchercraftModAttributes.SIGN_INTENSITY) ? _livingEntitySI.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).getValue() : 0) * 0.01);
			_vars.witchercraftQuenShield = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShieldMax;
			_vars.markSyncDirty();
		}
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.QUEN_EFFECT, 200, 0));
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class WhiteRaffardsDecoctionUsedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.WHITE_RAFFARDS_DECOCTION_EFFECT, 100, 0));
		if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("heal za" + ((entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5))), false);
		}
		if (entity instanceof LivingEntity _entity)
			_entity.setHealth((float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.5));
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftToxicity = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftToxicity + 25;
			_vars.markSyncDirty();
		}
		PotionUsedProcedure.execute(entity);
	}
}
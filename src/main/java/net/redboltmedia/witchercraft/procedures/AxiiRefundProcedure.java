package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.WitchercraftMod;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

public class AxiiRefundProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(WitchercraftModMobEffects.PLAYER_TRACKER, 5, 0));
		if (entity instanceof Player _player)
			_player.getFoodData().setFoodLevel((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) + 4);
		WitchercraftMod.queueServerWork(1, () -> {
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(WitchercraftModMobEffects.SIGN_COOLDOWN);
		});
	}
}
package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@EventBusSubscriber
public class SignCastHoldProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime > 20) {
			if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) > 4) {
				if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.IGNI_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesFireStream) {
					AltIgniCastProcedure.execute(entity);
				}
				if (entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(WitchercraftModMobEffects.QUEN_SIGN) && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesExploadingShield) {
					AltQuenCastProcedure.execute(entity);
				}
				if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSignKeyHoldTime % 20 == 0) {
					SignCastHoldCostTickProcedure.execute(entity);
				}
			} else {
				SignCastKeyReleaseProcedure.execute(world, x, y, z, entity);
				if (entity instanceof LivingEntity _entity)
					_entity.removeEffect(WitchercraftModMobEffects.SIGN_HOLD);
				if (entity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal("Not enough stamina hold "), true);
			}
		}
	}
}
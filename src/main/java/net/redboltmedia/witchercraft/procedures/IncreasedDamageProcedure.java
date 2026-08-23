package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class IncreasedDamageProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		double sumIncreasedDamage = 0;
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesStrengthTraining == true) {
			sumIncreasedDamage = sumIncreasedDamage + 10;
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAbilitiesSunderArmor == true) {
			sumIncreasedDamage = sumIncreasedDamage + 20;
		}
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.THUNDERBOLT_EFFECT)) {
			sumIncreasedDamage = sumIncreasedDamage + 20;
		}
		if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(WitchercraftModMobEffects.WATER_HAG_DECOCTION_EFFECT)
				&& (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) == (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)) {
			sumIncreasedDamage = sumIncreasedDamage + 40;
		}
		if (entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(WitchercraftModMobEffects.NEKKER_WARRIOR_DECOCTION_EFFECT) && entity.isPassenger()) {
			sumIncreasedDamage = sumIncreasedDamage + 50;
		}
		if (entity instanceof LivingEntity _livEnt6 && _livEnt6.hasEffect(WitchercraftModMobEffects.WYVERN_DECOCTION_EFFECT)) {
			sumIncreasedDamage = sumIncreasedDamage + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftWyvernDecoctionHit;
		}
		if (entity instanceof LivingEntity _livEnt7 && _livEnt7.hasEffect(WitchercraftModMobEffects.SUCCUBUS_DECOCTION_EFFECT)) {
			sumIncreasedDamage = sumIncreasedDamage + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftSuccubusDecoctionTick;
		}
		{
			WitchercraftModVariables.PlayerVariables _vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
			_vars.witchercraftIncreasedDamage = sumIncreasedDamage;
			_vars.markSyncDirty();
		}
	}
}
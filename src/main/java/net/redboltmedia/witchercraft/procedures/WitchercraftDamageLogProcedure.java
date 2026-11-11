package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class WitchercraftDamageLogProcedure {
	public static void execute(Entity sourceentity, double kind, double amount, double hit, double crit, double oil, double hitBonus, double takenBonus, double hitType, double executed) {
		if (sourceentity == null)
			return;
		double r = 0;
		double critShown = 0;
		if (sourceentity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WitchercraftModMobEffects.DEV_LOG)) {
			if (sourceentity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal("\u00A76----------------------------------------------"), false);
			critShown = crit;
			if (kind == 1) {
				if (hitType == 1) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("fast x" + Math.round(sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0)))), false);
				}
				if (hitType == 2) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal(("strong x" + Math.round(sourceentity.getPersistentData().getDoubleOr("witchercraftHitStreak", 0)))), false);
				}
				if (hitType == 0) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal("no type"), false);
				}
				r = Math.round(amount * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("base " + r / 1000)), false);
				r = Math.round((sourceentity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE)
						? _livingEntity1.getAttribute(WitchercraftModAttributes.ADDITIONAL_DAMAGE).getValue()
						: 0) * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("flat +" + r / 1000)), false);
				r = Math.round(oil * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("oil +" + r / 1000)), false);
				r = Math.round((sourceentity instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(WitchercraftModAttributes.INCREASED_DAMAGE)
						? _livingEntity2.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).getValue()
						: 0) * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("damage +" + r / 1000 + "%")), false);
				r = Math.round(hitBonus * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("hit bonus +" + r / 1000 + ("%" + (sourceentity.getPersistentData().getStringOr("witchercraftHitBonusLog", ""))))), false);
				r = Math.round(takenBonus * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("taken +" + r / 1000 + "%")), false);
				if (hitType == 2) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal("strong +50%"), false);
					critShown = critShown - 0.5;
				}
				r = Math.round(critShown * 100 * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("crit +" + r / 1000 + "%")), false);
				r = Math.round(hit * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("= " + r / 1000)), false);
				if (executed == 1) {
					if (sourceentity instanceof ServerPlayer _player)
						_player.sendSystemMessage(Component.literal("execute!"), false);
				}
			} else {
				r = Math.round(amount * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("arrow " + r / 1000)), false);
				r = Math.round(critShown * 100 * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("crit +" + r / 1000 + "%")), false);
				r = Math.round(hitBonus * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("arrow bonus +" + r / 1000 + "%")), false);
				r = Math.round(takenBonus * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("taken +" + r / 1000 + "%")), false);
				r = Math.round(hit * 1000);
				if (sourceentity instanceof ServerPlayer _player)
					_player.sendSystemMessage(Component.literal(("= " + r / 1000)), false);
			}
		}
	}
}

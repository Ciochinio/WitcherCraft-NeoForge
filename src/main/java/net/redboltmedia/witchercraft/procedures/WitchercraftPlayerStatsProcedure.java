package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class WitchercraftPlayerStatsProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(
					Component.literal(("Health" + (entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(Attributes.MAX_HEALTH) ? _livingEntity0.getAttribute(Attributes.MAX_HEALTH).getValue() : 0))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(
					Component.literal(
							("Movement Speed " + (entity instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED) ? _livingEntity2.getAttribute(Attributes.MOVEMENT_SPEED).getValue() : 0))),
					false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Armor" + (entity instanceof LivingEntity _livEnt ? _livEnt.getArmorValue() : 0))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(
					Component.literal(("Attack Speed" + (entity instanceof LivingEntity _livingEntity6 && _livingEntity6.getAttributes().hasAttribute(Attributes.ATTACK_SPEED) ? _livingEntity6.getAttribute(Attributes.ATTACK_SPEED).getValue() : 0))),
					false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Crit Rate"
					+ ((entity instanceof LivingEntity _livingEntity8 && _livingEntity8.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_CHANCE) ? _livingEntity8.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).getValue() : 0) + "%"))),
					false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Crit Damage"
					+ ((entity instanceof LivingEntity _livingEntity10 && _livingEntity10.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntity10.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0)
							+ "%"))),
					false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Passive Health Regeneration" + ((entity instanceof LivingEntity _livingEntity12 && _livingEntity12.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN)
					? _livingEntity12.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).getValue()
					: 0) + "Hp/2s"))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Passive Stamina Regeneration" + ((entity instanceof LivingEntity _livingEntity14 && _livingEntity14.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN)
					? _livingEntity14.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).getValue()
					: 0) + "Stamina/2s"))), false);
	}
}
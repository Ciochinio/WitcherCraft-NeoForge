package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;
import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class WitchercraftPlayerStatsProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Health" + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftHealth)), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Movement Speed " + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMovementSpeed)), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Armor" + (entity instanceof LivingEntity _livEnt ? _livEnt.getArmorValue() : 0))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Attack Speed" + entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftAttackSpeed)), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Crit Rate" + ((entity instanceof LivingEntity _livingEntityCC && _livingEntityCC.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_CHANCE) ? _livingEntityCC.getAttribute(WitchercraftModAttributes.CRIT_CHANCE).getValue() : 0) + "%"))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Crit Damage" + ((entity instanceof LivingEntity _livingEntityCD && _livingEntityCD.getAttributes().hasAttribute(WitchercraftModAttributes.CRIT_DAMAGE) ? _livingEntityCD.getAttribute(WitchercraftModAttributes.CRIT_DAMAGE).getValue() : 0) + "%"))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Passive Health Regeneration" + ((entity instanceof LivingEntity _livingEntityPHR && _livingEntityPHR.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN) ? _livingEntityPHR.getAttribute(WitchercraftModAttributes.PASSIVE_HEALTH_REGEN).getValue() : 0) + "Hp/2s"))), false);
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal(("Passive Stamina Regeneration" + ((entity instanceof LivingEntity _livingEntityPSR && _livingEntityPSR.getAttributes().hasAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN) ? _livingEntityPSR.getAttribute(WitchercraftModAttributes.PASSIVE_STAMINA_REGEN).getValue() : 0) + "Stamina/2s"))), false);
	}
}
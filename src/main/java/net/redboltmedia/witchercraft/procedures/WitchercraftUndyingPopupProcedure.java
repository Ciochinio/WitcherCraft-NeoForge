package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.WitchercraftUndyingMessage;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

// LOCKED: hand-written, Blockly cannot send a custom packet. Shows the Undying pop-up on the saved player's screen.
public class WitchercraftUndyingPopupProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer player)
			PacketDistributor.sendToPlayer(player, new WitchercraftUndyingMessage());
	}
}

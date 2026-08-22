package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class QuenActiveTickProcedure {
	// NOTE: code-locked (locked_code=true) so MCreator preserves the color styling.
	// The Blockly cannot represent per-player action-bar color; edit this Java directly.
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("Quen: " + Math.round(entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftQuenShield)).withStyle(ChatFormatting.GOLD), true);
	}
}

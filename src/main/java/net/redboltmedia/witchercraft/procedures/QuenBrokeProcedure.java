package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class QuenBrokeProcedure {
	// NOTE: code-locked (locked_code=true) so MCreator preserves the color styling.
	// The Blockly cannot represent per-player action-bar color; edit this Java directly.
	// Fires from QUEN_EFFECT's onExpired (both on shatter and on timeout).
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ServerPlayer _player)
			_player.sendSystemMessage(Component.literal("Quen broke!").withStyle(ChatFormatting.RED), true);
	}
}

package net.redboltmedia.witchercraft.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import net.redboltmedia.witchercraft.FastTravel;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/World Map/Fast Travel). Called by the signpost's right-click
 * Blockly procedure with the clicked half. On the server, starts a travel session at that signpost and
 * opens the map in travel mode for the player, or tells them why not. Does nothing on the client.
 */
public class FastTravelSignStartTravelProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!world.isClientSide() && entity instanceof ServerPlayer player)
			FastTravel.openFromSign(player, BlockPos.containing(x, y, z));
	}
}

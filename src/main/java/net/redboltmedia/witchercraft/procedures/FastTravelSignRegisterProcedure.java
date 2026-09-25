package net.redboltmedia.witchercraft.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import net.redboltmedia.witchercraft.FastTravelSigns;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/World Map/Fast Travel). Called by the
 * FastTravelSignPlaced Blockly procedure after a player places the lower half of a signpost.
 * Registers the shared fast-travel destination and opens the naming popup for the placer.
 * Returns false when the placement must be undone (outside the Overworld, no room for the upper
 * half, or the world's player-placed signpost limit is reached); the player is told why.
 */
public class FastTravelSignRegisterProcedure {
	public static boolean execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return false;
		return FastTravelSigns.registerPlaced(world, BlockPos.containing(x, y, z), entity);
	}
}

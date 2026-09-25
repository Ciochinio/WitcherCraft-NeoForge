package net.redboltmedia.witchercraft.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import net.redboltmedia.witchercraft.FastTravelSigns;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/World Map/Fast Travel). Called by the signpost's
 * destroy, explosion, and orphan-cleanup Blockly procedures after a half was removed. Deletes the
 * destination (and its map marker) of a sign anchored at this block or the block below that no
 * longer stands. Returns true only when a destination was removed, so exactly one call per
 * destroyed sign returns true and the caller drops at most one item.
 */
public class FastTravelSignRemoveProcedure {
	public static boolean execute(LevelAccessor world, double x, double y, double z) {
		return FastTravelSigns.removeIfGone(world, BlockPos.containing(x, y, z));
	}
}

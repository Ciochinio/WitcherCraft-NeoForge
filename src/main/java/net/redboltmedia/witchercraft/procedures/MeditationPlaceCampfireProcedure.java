package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). Places a campfire next
 * to the initiator when meditation starts (Witcher flavour) - but skips if a
 * campfire is already nearby, so it does not stack. Persistent (not removed
 * afterwards); infinite campfires are acceptable by design.
 *
 * NOTE: intended to be Blockly (see handoff). Java for now - the
 * skip-if-present scan + floor search is loop-heavy to hand-author as blocks
 * without MCreator open. The block ops map to the world_data_isair /
 * block_replace blocks when converted.
 */
public class MeditationPlaceCampfireProcedure {
	/** Radius (blocks) scanned for an existing campfire before placing a new one. */
	private static final int SCAN = 4;
	// candidate spots around the player's feet: the four cardinal neighbours
	private static final int[][] SPOT_OFFSETS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {0, 0}};

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(world instanceof ServerLevel level))
			return;
		BlockPos base = BlockPos.containing(x, y, z);

		// already a campfire nearby? then don't place another.
		for (BlockPos p : BlockPos.betweenClosed(base.offset(-SCAN, -1, -SCAN), base.offset(SCAN, 1, SCAN))) {
			if (level.getBlockState(p).is(Blocks.CAMPFIRE) || level.getBlockState(p).is(Blocks.SOUL_CAMPFIRE))
				return;
		}

		// find an open cell with a solid floor beneath, next to the player.
		for (int[] o : SPOT_OFFSETS) {
			BlockPos spot = base.offset(o[0], 0, o[1]);
			if (level.isEmptyBlock(spot) && !level.isEmptyBlock(spot.below())) {
				level.setBlock(spot, Blocks.CAMPFIRE.defaultBlockState(), 3);
				return;
			}
		}
	}
}

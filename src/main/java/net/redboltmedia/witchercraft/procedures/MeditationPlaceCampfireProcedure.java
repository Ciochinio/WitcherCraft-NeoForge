package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). Places a campfire in
 * FRONT of the initiator (Witcher flavour) - but skips if a campfire is already
 * nearby, so it does not stack. Persistent (not removed afterwards); infinite
 * campfires are acceptable by design.
 *
 * Candidates are ordered by facing (front, then the two sides, then behind) so
 * it lands somewhere the player is actually looking, not a fixed compass
 * direction; the player's own block is never a candidate (an earlier version
 * could pick the player's own feet and set them on fire).
 *
 * NOTE: intended to be Blockly (see handoff). Java for now - the
 * skip-if-present scan + floor search is loop-heavy to hand-author as blocks
 * without MCreator open. The block ops map to the world_data_isair /
 * block_replace blocks when converted.
 */
public class MeditationPlaceCampfireProcedure {
	/** Radius (blocks) scanned for an existing campfire before placing a new one. */
	private static final int SCAN = 4;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(world instanceof ServerLevel level))
			return;
		BlockPos base = BlockPos.containing(x, y, z);

		// already a campfire nearby? then don't place another.
		for (BlockPos p : BlockPos.betweenClosed(base.offset(-SCAN, -1, -SCAN), base.offset(SCAN, 1, SCAN))) {
			if (level.getBlockState(p).is(Blocks.CAMPFIRE) || level.getBlockState(p).is(Blocks.SOUL_CAMPFIRE))
				return;
		}

		// candidates: front, right, left, behind - never the player's own block.
		Direction front = entity.getDirection();
		Direction[] order = {front, front.getClockWise(), front.getCounterClockWise(), front.getOpposite()};

		for (Direction dir : order) {
			BlockPos spot = base.relative(dir);
			if (level.isEmptyBlock(spot) && !level.isEmptyBlock(spot.below())) {
				level.setBlock(spot, Blocks.CAMPFIRE.defaultBlockState(), 3);
				return;
			}
		}
	}
}

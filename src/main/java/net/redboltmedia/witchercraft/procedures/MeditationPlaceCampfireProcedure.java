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
 * Candidates are ordered by facing (front first, then front diagonals, then the
 * sides, then behind) so it lands where the player is actually looking, not a
 * fixed compass direction. Every candidate is at least MIN_DIST blocks away so
 * the campfire never spawns on top of the player (adjacent campfires burn).
 *
 * NOTE: intended to be Blockly (see handoff). Java for now - the
 * skip-if-present scan + floor search is loop-heavy to hand-author as blocks
 * without MCreator open. The block ops map to the world_data_isair /
 * block_replace blocks when converted.
 */
public class MeditationPlaceCampfireProcedure {
	/** Radius (blocks) scanned for an existing campfire before placing a new one. */
	private static final int SCAN = 4;
	/** Minimum distance (blocks) from the player - closer than this and the fire burns them. */
	private static final int MIN_DIST = 2;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(world instanceof ServerLevel level))
			return;
		BlockPos base = BlockPos.containing(x, y, z);

		// already a campfire nearby? then don't place another.
		for (BlockPos p : BlockPos.betweenClosed(base.offset(-SCAN, -1, -SCAN), base.offset(SCAN, 1, SCAN))) {
			if (level.getBlockState(p).is(Blocks.CAMPFIRE) || level.getBlockState(p).is(Blocks.SOUL_CAMPFIRE))
				return;
		}

		// candidates, all >= MIN_DIST blocks away, ordered by facing:
		// straight ahead (2 then 3), the two front diagonals, the sides, then behind.
		Direction front = entity.getDirection();
		Direction right = front.getClockWise();
		Direction left = front.getCounterClockWise();
		BlockPos ahead = base.relative(front, MIN_DIST);
		BlockPos[] candidates = {
				ahead,
				base.relative(front, MIN_DIST + 1),
				ahead.relative(right),
				ahead.relative(left),
				base.relative(right, MIN_DIST),
				base.relative(left, MIN_DIST),
				base.relative(front.getOpposite(), MIN_DIST),
		};

		for (BlockPos spot : candidates) {
			if (level.isEmptyBlock(spot) && !level.isEmptyBlock(spot.below())) {
				level.setBlock(spot, Blocks.CAMPFIRE.defaultBlockState(), 3);
				return;
			}
		}
	}
}

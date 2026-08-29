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

		// candidate COLUMNS, all >= MIN_DIST blocks away horizontally, ordered by
		// facing: straight ahead (2 then 3), the two front diagonals, the sides, then
		// behind. Only the height is searched per column (see findFloorSpot), so the
		// campfire is never placed closer than MIN_DIST to the player - never under them.
		Direction front = entity.getDirection();
		Direction right = front.getClockWise();
		Direction left = front.getCounterClockWise();
		BlockPos ahead = base.relative(front, MIN_DIST);
		BlockPos[] columns = {
				ahead,
				base.relative(front, MIN_DIST + 1),
				ahead.relative(right),
				ahead.relative(left),
				base.relative(right, MIN_DIST),
				base.relative(left, MIN_DIST),
				base.relative(front.getOpposite(), MIN_DIST),
		};

		for (BlockPos col : columns) {
			BlockPos spot = findFloorSpot(level, col);
			if (spot != null) {
				level.setBlock(spot, Blocks.CAMPFIRE.defaultBlockState(), 3);
				return;
			}
		}
	}

	/**
	 * In a column at the player's feet level, search a small vertical window (a few
	 * blocks up and down, to cope with slopes/steps) for an open cell with a solid
	 * floor beneath. Only Y changes here - the horizontal offset is fixed by the
	 * caller (>= MIN_DIST), so this can never bring the campfire under the player.
	 */
	private static BlockPos findFloorSpot(ServerLevel level, BlockPos col) {
		for (int dy = 2; dy >= -3; dy--) {
			BlockPos p = col.offset(0, dy, 0);
			if (level.isEmptyBlock(p) && !level.isEmptyBlock(p.below()))
				return p;
		}
		return null;
	}
}

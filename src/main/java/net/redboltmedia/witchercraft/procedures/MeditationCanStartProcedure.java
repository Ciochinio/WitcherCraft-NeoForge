package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). The meditation safety
 * gate, re-validated server-side before a session starts: you must have open
 * space around you (not meditating boxed in a 1x1 hole) and no hostile monster
 * nearby (safe, like sleeping).
 *
 * NOTE: this was meant to be a Blockly procedure. It is Java for now because a
 * robust "any monster in radius" scan + multi-block air check can't be safely
 * hand-authored as Blockly XML without MCreator open to verify the blocks. See
 * the handoff plan - converting it to blocks (world_data_isair +
 * world_entity_inrange_* ) is the first task.
 */
public class MeditationCanStartProcedure {
	/** Open-air cells required around the head (of the 6 checked) - "not boxed in". */
	private static final int MIN_AIR = 5;
	/** No hostile monster may be within this radius (blocks). */
	private static final double MONSTER_RADIUS = 12.0;

	// head-level ring + one above: enough openness to rule out a 1x1 hole
	private static final int[][] AIR_OFFSETS = {{0, 1, 0}, {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1}, {0, 2, 0}};

	public static boolean execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(world instanceof ServerLevel level))
			return false;
		BlockPos base = BlockPos.containing(x, y, z);

		int air = 0;
		for (int[] o : AIR_OFFSETS)
			if (level.isEmptyBlock(base.offset(o[0], o[1], o[2])))
				air++;
		if (air < MIN_AIR)
			return false;

		AABB box = new AABB(base).inflate(MONSTER_RADIUS);
		return level.getEntitiesOfClass(Monster.class, box, Entity::isAlive).isEmpty();
	}
}

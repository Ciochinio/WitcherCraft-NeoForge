package net.redboltmedia.witchercraft.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). The meditation safety
 * gate, re-validated server-side before a session starts: you must have open
 * space around you (not meditating boxed in a 1x1 hole) and no monster nearby,
 * using exactly the vanilla bed rule ({@link net.redboltmedia.witchercraft.NearbyMonsters}).
 *
 * NOTE: this was meant to be a Blockly procedure. It is Java for now because a
 * robust "any monster in radius" scan + multi-block air check can't be safely
 * hand-authored as Blockly XML without MCreator open to verify the blocks. See
 * the handoff plan - converting it to blocks (world_data_isair +
 * world_entity_inrange_* ) is the first task.
 */
public class MeditationCanStartProcedure {
	/** Reason codes returned by {@link #reason} (shared with the reject packet + client message). */
	public static final int OK = 0;
	public static final int BLOCKED_SPACE = 1;
	public static final int BLOCKED_MONSTER = 2;
	public static final int BLOCKED_STAMINA = 3;

	/** Open-air cells required around the head (of the 6 checked) - "not boxed in". */
	private static final int MIN_AIR = 5;
	// head-level ring + one above: enough openness to rule out a 1x1 hole
	private static final int[][] AIR_OFFSETS = {{0, 1, 0}, {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1}, {0, 2, 0}};

	/** OK, or the first failing gate (space then monsters) so the caller can tell the player why. */
	public static int reason(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(world instanceof ServerLevel level))
			return BLOCKED_SPACE;
		BlockPos base = BlockPos.containing(x, y, z);

		int air = 0;
		for (int[] o : AIR_OFFSETS)
			if (level.isEmptyBlock(base.offset(o[0], o[1], o[2])))
				air++;
		if (air < MIN_AIR)
			return BLOCKED_SPACE;

		Vec3 center = Vec3.atBottomCenterOf(base);
		boolean monsters = entity instanceof Player player
			? net.redboltmedia.witchercraft.NearbyMonsters.preventRest(level, player, center)
			: net.redboltmedia.witchercraft.NearbyMonsters.anyMonster(level, center);
		if (monsters)
			return BLOCKED_MONSTER;

		return OK;
	}

	public static boolean execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		return reason(world, x, y, z, entity) == OK;
	}
}

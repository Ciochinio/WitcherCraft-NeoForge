package net.redboltmedia.witchercraft;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). The fast-travel price and exact raw-XP
 * arithmetic, shared by the server and the client's price display so both always agree.
 *
 * Price: base + one point per started stretch of "blocks per XP" of horizontal distance between the two
 * sign anchors, capped when a cap is set, zero with free travel or in creative mode.
 *
 * XP is handled as whole points. The debit sets level and progress directly, so it never fires the
 * vanilla XP-change event (which WitcherCraft levelling listens to) and has no float drift.
 */
public final class FastTravelCosts {
	private FastTravelCosts() {
	}

	public static int price(BlockPos from, BlockPos to, boolean creative) {
		if (WorldMapServerConfig.freeTravel() || creative)
			return 0;
		long dx = (long) to.getX() - from.getX();
		long dz = (long) to.getZ() - from.getZ();
		long cost = WorldMapServerConfig.baseXpCost() + startedStretches(dx * dx + dz * dz, WorldMapServerConfig.blocksPerXp());
		int cap = WorldMapServerConfig.maximumXpCost();
		if (cap > 0)
			cost = Math.min(cost, cap);
		return (int) Math.min(cost, Integer.MAX_VALUE);
	}

	/** The smallest k with k * stretch >= sqrt(distanceSquared), computed exactly. */
	static long startedStretches(long distanceSquared, int stretch) {
		long k = (long) Math.ceil(Math.sqrt((double) distanceSquared) / stretch);
		while (k > 0 && (k - 1) * stretch * (k - 1) * stretch >= distanceSquared)
			k--;
		while (k * stretch * k * stretch < distanceSquared)
			k++;
		return k;
	}

	/** Points needed to go from {@code level} to the next level (vanilla formula). */
	public static long pointsForLevel(int level) {
		if (level >= 30)
			return 112L + (level - 30L) * 9L;
		return level >= 15 ? 37L + (level - 15L) * 5L : 7L + level * 2L;
	}

	/** Total points needed to reach {@code level} from zero (vanilla's closed forms). */
	public static long pointsToReach(int level) {
		long l = Math.max(0, level);
		if (l <= 16)
			return l * l + 6 * l;
		if (l <= 31)
			return (5 * l * l - 81 * l + 720) / 2;
		return (9 * l * l - 325 * l + 4440) / 2;
	}

	/** Every raw XP point the player could spend: all completed levels plus the points inside the current one. */
	public static long spendablePoints(Player player) {
		return pointsToReach(player.experienceLevel) + (long) Math.floor(player.experienceProgress * pointsForLevel(player.experienceLevel));
	}

	/** Removes exactly {@code points} raw XP. The caller has checked {@link #spendablePoints}. */
	public static void debit(ServerPlayer player, int points) {
		if (points <= 0)
			return;
		long remaining = Math.max(0, spendablePoints(player) - points);
		int low = 0, high = Math.max(0, player.experienceLevel);
		while (low < high) {
			int middle = (int) (((long) low + high + 1) / 2);
			if (pointsToReach(middle) <= remaining)
				low = middle;
			else
				high = middle - 1;
		}
		player.setExperienceLevels(low);
		player.setExperiencePoints((int) (remaining - pointsToReach(low)));
		player.totalExperience = Math.max(0, player.totalExperience - points);
	}
}

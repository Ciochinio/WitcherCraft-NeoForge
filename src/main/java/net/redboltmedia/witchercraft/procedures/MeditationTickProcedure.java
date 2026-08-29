package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Holder;

import java.util.Optional;

/**
 * HAND-MAINTAINED (locked_code procedure, ~/Meditation2). Drives the accelerated
 * meditation time-lapse. Called every server tick from the TimeTick heartbeat
 * (once per online player); it is IDEMPOTENT per tick - it recomputes the
 * desired clock from the session anchors ({@link WitchercraftModVariables}), so
 * being invoked several times in the same tick never over-advances.
 *
 * The real default-clock is swept from the anchor forward by DELTA ticks over
 * {@link #DURATION_TICKS} real ticks; on arrival it snaps to the exact target
 * and clears the session. Insomnia (timeSinceRest) accrual lands in slice 2b.
 */
public class MeditationTickProcedure {
	/**
	 * Real ticks a FULL 24h (24000-tick) jump takes; a shorter jump scales down
	 * proportionally, so the spin duration tracks how much time is being skipped.
	 * Tunable - 180 ticks ~ 9 seconds for a whole day.
	 */
	public static final int FULL_DAY_SPIN_TICKS = 180;

	/** Real ticks the spin should take for a forward jump of {@code delta} clock ticks. */
	public static int spinDurationTicks(long delta) {
		long d = Math.max(0L, Math.min(24000L, delta));
		return Math.max(2, (int) Math.round(d / 24000.0 * FULL_DAY_SPIN_TICKS));
	}

	public static void execute(LevelAccessor world, Entity entity) {
		if (!(world instanceof ServerLevel level))
			return;
		if (WitchercraftModVariables.meditationState != 2)
			return;

		long anchor = (long) WitchercraftModVariables.meditationAnchorTicks;
		long delta = (long) WitchercraftModVariables.meditationDeltaTicks;
		long anchorGt = (long) WitchercraftModVariables.meditationAnchorGametime;

		long elapsed = level.getGameTime() - anchorGt;
		if (elapsed < 0)
			elapsed = 0;
		double frac = Math.min(1.0, elapsed / (double) spinDurationTicks(delta));
		setClock(level, anchor + (long) (delta * frac));

		if (frac >= 1.0) {
			setClock(level, anchor + delta); // land exactly on target
			WitchercraftModVariables.meditationState = 0;
		}
	}

	private static void setClock(ServerLevel level, long ticks) {
		ServerClockManager clockManager = level.getServer().clockManager();
		Optional<Holder<WorldClock>> clock = level.dimensionType().defaultClock();
		if (clock.isPresent())
			clockManager.setTotalTicks(clock.get(), ticks);
	}
}

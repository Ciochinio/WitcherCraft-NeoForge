package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.stats.Stats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Holder;

import java.util.Optional;
import java.util.UUID;

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
	/** Real ticks the SHORTEST jump still takes, so a small skip is a spin not a blink. ~2s. */
	public static final int MIN_SPIN_TICKS = 40;
	/**
	 * Real ticks a FULL 24h (24000-tick) jump takes. The duration scales linearly
	 * from MIN_SPIN_TICKS (tiny jump) to this (full day). Both tunable - 180 ~ 9s.
	 */
	public static final int FULL_DAY_SPIN_TICKS = 180;

	/** Real ticks the spin should take for a forward jump of {@code delta} clock ticks. */
	public static int spinDurationTicks(long delta) {
		long d = Math.max(0L, Math.min(24000L, delta));
		return (int) Math.round(MIN_SPIN_TICKS + (FULL_DAY_SPIN_TICKS - MIN_SPIN_TICKS) * (d / 24000.0));
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
			// insomnia: meditation never rests you, so advance the initiator's
			// time_since_rest by the ticks we skipped - phantoms still come.
			UUID id = MeditationStartProcedure.initiator;
			if (id != null) {
				if (level.getServer().getPlayerList().getPlayer(id) instanceof ServerPlayer sp)
					sp.awardStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST), (int) delta);
				MeditationStartProcedure.initiator = null;
			}
		}
	}

	private static void setClock(ServerLevel level, long ticks) {
		ServerClockManager clockManager = level.getServer().clockManager();
		Optional<Holder<WorldClock>> clock = level.dimensionType().defaultClock();
		if (clock.isPresent())
			clockManager.setTotalTicks(clock.get(), ticks);
	}
}

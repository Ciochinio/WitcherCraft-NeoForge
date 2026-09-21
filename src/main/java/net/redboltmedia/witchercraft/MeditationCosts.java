package net.redboltmedia.witchercraft;

/** Shared server and client calculations for meditation stamina prices. */
public final class MeditationCosts {
	public static final long TICKS_PER_HOUR = 1000L;

	private MeditationCosts() {}

	public static int timeCost(long elapsedClockTicks) {
		if (WorldMapServerConfig.freeMeditation() || elapsedClockTicks <= 0)
			return 0;
		long ticksPerStep = (long) WorldMapServerConfig.meditationHoursPerCostStep() * TICKS_PER_HOUR;
		long steps = (elapsedClockTicks + ticksPerStep - 1L) / ticksPerStep;
		long cost = steps * WorldMapServerConfig.meditationCostPerStep();
		return (int) Math.min(WorldMapServerConfig.meditationMaximumTimeCost(), cost);
	}

	public static int totalCost(long elapsedClockTicks) {
		return totalCost(elapsedClockTicks, false);
	}

	public static int totalCost(long elapsedClockTicks, boolean campfireNearby) {
		return WorldMapServerConfig.freeMeditation() ? 0 : (campfireNearby ? 0 : WorldMapServerConfig.meditationSetupCost()) + timeCost(elapsedClockTicks);
	}
}

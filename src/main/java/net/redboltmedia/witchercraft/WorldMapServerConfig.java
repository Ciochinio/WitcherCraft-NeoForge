package net.redboltmedia.witchercraft;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Per-world authority settings for the world map. Hard safety ceilings remain code constants. */
public final class WorldMapServerConfig {
	private static final boolean DEFAULT_MAP_ENABLED = true;
	private static final boolean DEFAULT_POIS_ENABLED = true;
	private static final double DEFAULT_REVEAL_RADIUS_VALUE = 256.0;
	private static final double DEFAULT_DISCOVERY_RADIUS_VALUE = 32.0;
	private static final int DEFAULT_REFRESH_COOLDOWN_TICKS = 1200;
	private static final int DEFAULT_MAX_CAPTURES_PER_TICK = 8;
	private static final int DEFAULT_CAPTURE_BUDGET_MICROS = 3000;
	private static final boolean DEFAULT_FREE_MEDITATION = false;
	private static final int DEFAULT_MEDITATION_SETUP_COST = 2;
	private static final int DEFAULT_MEDITATION_HOURS_PER_COST_STEP = 4;
	private static final int DEFAULT_MEDITATION_COST_PER_STEP = 2;
	private static final int DEFAULT_MEDITATION_MAXIMUM_TIME_COST = 10;
	public static final int HARD_MAX_CAPTURES_PER_TICK = 32;
	public static final int HARD_MAX_CAPTURE_BUDGET_MICROS = 20_000;
	public static final int HARD_MAX_REFRESH_COOLDOWN_TICKS = 1_728_000;

	private static final ModConfigSpec SPEC;
	private static final ModConfigSpec.BooleanValue MAP_ENABLED;
	private static final ModConfigSpec.BooleanValue POIS_ENABLED;
	private static final ModConfigSpec.BooleanValue MINIMAP_ENABLED;
	private static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_POI_DEFINITIONS;
	private static final ModConfigSpec.DoubleValue DEFAULT_REVEAL_RADIUS;
	private static final ModConfigSpec.DoubleValue DEFAULT_DISCOVERY_RADIUS;
	private static final ModConfigSpec.IntValue TILE_REFRESH_COOLDOWN_TICKS;
	private static final ModConfigSpec.IntValue MAX_CAPTURES_PER_TICK;
	private static final ModConfigSpec.IntValue CAPTURE_BUDGET_MICROS;
	private static final ModConfigSpec.IntValue WAYPOINT_LIMIT;
	private static final ModConfigSpec.BooleanValue FREE_MEDITATION;
	private static final ModConfigSpec.IntValue MEDITATION_SETUP_COST;
	private static final ModConfigSpec.IntValue MEDITATION_HOURS_PER_COST_STEP;
	private static final ModConfigSpec.IntValue MEDITATION_COST_PER_STEP;
	private static final ModConfigSpec.IntValue MEDITATION_MAXIMUM_TIME_COST;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		builder.comment("Per-world gameplay and workload settings for the WitcherCraft world map.")
			.translation("witchercraft.configuration.world_map").push("worldMap");
		MAP_ENABLED = option(builder, "map_enabled", "Enable world-map capture, transfer, waypoints, and POIs for this world.").define("mapEnabled", DEFAULT_MAP_ENABLED);
		POIS_ENABLED = option(builder, "pois_enabled", "Enable world-map POI observation, discovery, and transfer.").worldRestart().define("poisEnabled", DEFAULT_POIS_ENABLED);
		ENABLED_POI_DEFINITIONS = option(builder, "enabled_poi_definitions", "POI definition identifiers allowed in this world. An empty list enables every valid definition.")
			.worldRestart().defineListAllowEmpty("enabledPoiDefinitions", List.of(), () -> "witchercraft:poi_test_structure", WorldMapServerConfig::validIdentifier);
		DEFAULT_REVEAL_RADIUS = option(builder, "default_reveal_radius", "Reveal radius used when a POI JSON definition omits reveal_radius.").worldRestart().defineInRange("defaultRevealRadius", DEFAULT_REVEAL_RADIUS_VALUE, 0.0, WorldMapPoiDefinition.MAX_RADIUS);
		DEFAULT_DISCOVERY_RADIUS = option(builder, "default_discovery_radius", "Discovery radius used when a POI JSON definition omits discovery_radius.").worldRestart().defineInRange("defaultDiscoveryRadius", DEFAULT_DISCOVERY_RADIUS_VALUE, 0.0, WorldMapPoiDefinition.MAX_RADIUS);
		TILE_REFRESH_COOLDOWN_TICKS = option(builder, "tile_refresh_cooldown_ticks", "Minimum ticks before a watched chunk is recaptured during the same server session.").defineInRange("tileRefreshCooldownTicks", DEFAULT_REFRESH_COOLDOWN_TICKS, 0, HARD_MAX_REFRESH_COOLDOWN_TICKS);
		MAX_CAPTURES_PER_TICK = option(builder, "max_captures_per_tick", "Maximum loaded chunks sampled during one server tick.").defineInRange("maxCapturesPerTick", DEFAULT_MAX_CAPTURES_PER_TICK, 1, HARD_MAX_CAPTURES_PER_TICK);
		CAPTURE_BUDGET_MICROS = option(builder, "capture_budget_micros", "Approximate per-tick terrain-capture time budget in microseconds after the first capture.").defineInRange("captureBudgetMicros", DEFAULT_CAPTURE_BUDGET_MICROS, 250, HARD_MAX_CAPTURE_BUDGET_MICROS);
		WAYPOINT_LIMIT = option(builder, "waypoint_limit", "Maximum personal waypoints each player may create. Lowering this never deletes existing waypoints.").defineInRange("waypointLimit", WorldMapWaypoints.DEFAULT_WAYPOINT_LIMIT, 0, WorldMapWaypoints.HARD_MAX_WAYPOINTS_PER_PLAYER);
		builder.pop();
		builder.comment("Per-world permissions for the WitcherCraft minimap.")
			.translation("witchercraft.configuration.minimap").push("minimap");
		MINIMAP_ENABLED = minimapOption(builder, "enabled", "Allow players to display the WitcherCraft minimap in this world.").define("enabled", true);
		builder.pop();
		builder.comment("Per-world gameplay costs for meditation.")
			.translation("witchercraft.configuration.meditation").push("meditation");
		FREE_MEDITATION = meditationOption(builder, "free", "Disable all meditation stamina costs.").define("freeMeditation", DEFAULT_FREE_MEDITATION);
		MEDITATION_SETUP_COST = meditationOption(builder, "setup_cost", "Stamina charged when an accepted meditation begins.").defineInRange("setupCost", DEFAULT_MEDITATION_SETUP_COST, 0, 20);
		MEDITATION_HOURS_PER_COST_STEP = meditationOption(builder, "hours_per_cost_step", "Meditated hours represented by one time-cost step.").defineInRange("hoursPerCostStep", DEFAULT_MEDITATION_HOURS_PER_COST_STEP, 1, 24);
		MEDITATION_COST_PER_STEP = meditationOption(builder, "cost_per_step", "Stamina charged for each started time-cost step.").defineInRange("costPerStep", DEFAULT_MEDITATION_COST_PER_STEP, 0, 20);
		MEDITATION_MAXIMUM_TIME_COST = meditationOption(builder, "maximum_time_cost", "Maximum stamina charged for elapsed meditation time, excluding setup.").defineInRange("maximumTimeCost", DEFAULT_MEDITATION_MAXIMUM_TIME_COST, 0, 20);
		builder.pop();
		SPEC = builder.build();
	}

	private WorldMapServerConfig() {}

	private static ModConfigSpec.Builder option(ModConfigSpec.Builder builder, String key, String comment) {
		return builder.comment(comment).translation("witchercraft.configuration.world_map." + key);
	}

	private static ModConfigSpec.Builder minimapOption(ModConfigSpec.Builder builder, String key, String comment) {
		return builder.comment(comment).translation("witchercraft.configuration.minimap." + key);
	}

	private static ModConfigSpec.Builder meditationOption(ModConfigSpec.Builder builder, String key, String comment) {
		return builder.comment(comment).translation("witchercraft.configuration.meditation." + key);
	}

	private static boolean validIdentifier(Object value) {
		return value instanceof String string && Identifier.tryParse(string) != null;
	}

	public static void register() {
		ModContainer container = ModList.get().getModContainerById(WitchercraftMod.MODID)
			.orElseThrow(() -> new IllegalStateException("WitcherCraft mod container is unavailable during server-config registration"));
		container.registerConfig(ModConfig.Type.SERVER, SPEC);
	}

	public static boolean loaded() { return SPEC.isLoaded(); }
	public static boolean mapEnabled() { return !loaded() ? DEFAULT_MAP_ENABLED : MAP_ENABLED.getAsBoolean(); }
	public static boolean poisEnabled() { return mapEnabled() && (!loaded() ? DEFAULT_POIS_ENABLED : POIS_ENABLED.getAsBoolean()); }
	public static boolean minimapEnabled() { return mapEnabled() && (!loaded() || MINIMAP_ENABLED.getAsBoolean()); }
	public static Set<Identifier> enabledPoiDefinitions() {
		if (!loaded()) return Set.of();
		return ENABLED_POI_DEFINITIONS.get().stream().limit(WorldMapPoiDefinition.MAX_DEFINITIONS).map(Identifier::tryParse).filter(java.util.Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}
	public static boolean poiDefinitionEnabled(Identifier id) {
		Set<Identifier> enabled = enabledPoiDefinitions();
		return enabled.isEmpty() || enabled.contains(id);
	}
	public static double defaultRevealRadius() { return !loaded() ? DEFAULT_REVEAL_RADIUS_VALUE : DEFAULT_REVEAL_RADIUS.getAsDouble(); }
	public static double defaultDiscoveryRadius() { return !loaded() ? DEFAULT_DISCOVERY_RADIUS_VALUE : DEFAULT_DISCOVERY_RADIUS.getAsDouble(); }
	public static int tileRefreshCooldownTicks() { return !loaded() ? DEFAULT_REFRESH_COOLDOWN_TICKS : TILE_REFRESH_COOLDOWN_TICKS.getAsInt(); }
	public static int maxCapturesPerTick() { return !loaded() ? DEFAULT_MAX_CAPTURES_PER_TICK : MAX_CAPTURES_PER_TICK.getAsInt(); }
	public static long captureBudgetNanos() { return (!loaded() ? DEFAULT_CAPTURE_BUDGET_MICROS : CAPTURE_BUDGET_MICROS.getAsInt()) * 1_000L; }
	public static int waypointLimit() { return !loaded() ? WorldMapWaypoints.DEFAULT_WAYPOINT_LIMIT : WAYPOINT_LIMIT.getAsInt(); }
	public static boolean freeMeditation() { return loaded() ? FREE_MEDITATION.getAsBoolean() : DEFAULT_FREE_MEDITATION; }
	public static int meditationSetupCost() { return loaded() ? MEDITATION_SETUP_COST.getAsInt() : DEFAULT_MEDITATION_SETUP_COST; }
	public static int meditationHoursPerCostStep() { return loaded() ? MEDITATION_HOURS_PER_COST_STEP.getAsInt() : DEFAULT_MEDITATION_HOURS_PER_COST_STEP; }
	public static int meditationCostPerStep() { return loaded() ? MEDITATION_COST_PER_STEP.getAsInt() : DEFAULT_MEDITATION_COST_PER_STEP; }
	public static int meditationMaximumTimeCost() { return loaded() ? MEDITATION_MAXIMUM_TIME_COST.getAsInt() : DEFAULT_MEDITATION_MAXIMUM_TIME_COST; }
}

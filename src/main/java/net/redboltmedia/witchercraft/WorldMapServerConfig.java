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
	private static final boolean DEFAULT_FAST_TRAVEL_ENABLED = true;
	private static final boolean DEFAULT_SIGN_DROPS_ITEM = true;
	private static final int DEFAULT_PLAYER_SIGN_LIMIT = 256;
	private static final boolean DEFAULT_SHARED_SIGN_DISCOVERY = false;
	private static final String DEFAULT_MAP_NAME_LANGUAGE = "";
	private static final boolean DEFAULT_FREE_TRAVEL = false;
	private static final int DEFAULT_BASE_XP_COST = 5;
	private static final int DEFAULT_BLOCKS_PER_XP = 100;
	private static final int DEFAULT_MAXIMUM_XP_COST = 0;
	private static final boolean DEFAULT_BLOCK_TRAVEL_IN_COMBAT = true;
	private static final boolean DEFAULT_BLOCK_TRAVEL_NEAR_MONSTERS = true;
	public static final int HARD_MAX_TRAVEL_XP = 100_000;
	public static final int HARD_MAX_PLAYER_SIGN_LIMIT = 100_000;
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
	private static final ModConfigSpec.BooleanValue FAST_TRAVEL_ENABLED;
	private static final ModConfigSpec.BooleanValue SIGN_DROPS_ITEM;
	private static final ModConfigSpec.IntValue PLAYER_SIGN_LIMIT;
	private static final ModConfigSpec.BooleanValue SHARED_SIGN_DISCOVERY;
	private static final ModConfigSpec.ConfigValue<String> MAP_NAME_LANGUAGE;
	private static final ModConfigSpec.BooleanValue FREE_TRAVEL;
	private static final ModConfigSpec.IntValue BASE_XP_COST;
	private static final ModConfigSpec.IntValue BLOCKS_PER_XP;
	private static final ModConfigSpec.IntValue MAXIMUM_XP_COST;
	private static final ModConfigSpec.BooleanValue BLOCK_TRAVEL_IN_COMBAT;
	private static final ModConfigSpec.BooleanValue BLOCK_TRAVEL_NEAR_MONSTERS;

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
		MAP_NAME_LANGUAGE = option(builder, "map_name_language", "Language code, such as pl_pl, used for generated place names on the map for every player. Empty uses each player's own game language. Interface text always follows the player's language.")
			.define("mapNameLanguage", DEFAULT_MAP_NAME_LANGUAGE, WorldMapServerConfig::validLanguageCode);
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
		builder.comment("Per-world rules for fast-travel signposts.")
			.translation("witchercraft.configuration.fast_travel").push("fastTravel");
		FAST_TRAVEL_ENABLED = fastTravelOption(builder, "enabled", "Enable fast-travel signpost markers and travel. Disabling keeps signs, names, and discoveries.").worldRestart().define("enabled", DEFAULT_FAST_TRAVEL_ENABLED);
		SIGN_DROPS_ITEM = fastTravelOption(builder, "sign_drops_item", "Destroyed signposts drop a placeable signpost item outside creative mode.").define("signDropsItem", DEFAULT_SIGN_DROPS_ITEM);
		PLAYER_SIGN_LIMIT = fastTravelOption(builder, "player_sign_limit", "Maximum player-placed signposts in this world. Zero means no limit. Village signposts do not count. Lowering this never removes signs.").defineInRange("playerSignLimit", DEFAULT_PLAYER_SIGN_LIMIT, 0, HARD_MAX_PLAYER_SIGN_LIMIT);
		SHARED_SIGN_DISCOVERY = fastTravelOption(builder, "shared_discovery", "A signpost discovered by any player counts as discovered for every player. Switching this off returns everyone to their own discoveries.").define("sharedDiscovery", DEFAULT_SHARED_SIGN_DISCOVERY);
		FREE_TRAVEL = fastTravelOption(builder, "free_travel", "Fast travel costs no XP. Every other travel rule still applies.").define("freeTravel", DEFAULT_FREE_TRAVEL);
		BASE_XP_COST = fastTravelOption(builder, "base_xp_cost", "Flat raw XP points charged for every journey.").defineInRange("baseXpCost", DEFAULT_BASE_XP_COST, 0, HARD_MAX_TRAVEL_XP);
		BLOCKS_PER_XP = fastTravelOption(builder, "blocks_per_xp", "One more XP point is charged for every started stretch of this many blocks of horizontal distance.").defineInRange("blocksPerXp", DEFAULT_BLOCKS_PER_XP, 1, HARD_MAX_TRAVEL_XP);
		MAXIMUM_XP_COST = fastTravelOption(builder, "maximum_xp_cost", "Highest XP price of one journey. Zero means no cap.").defineInRange("maximumXpCost", DEFAULT_MAXIMUM_XP_COST, 0, HARD_MAX_TRAVEL_XP);
		BLOCK_TRAVEL_IN_COMBAT = fastTravelOption(builder, "block_in_combat", "Players who are in combat cannot set off.").define("blockInCombat", DEFAULT_BLOCK_TRAVEL_IN_COMBAT);
		BLOCK_TRAVEL_NEAR_MONSTERS = fastTravelOption(builder, "block_near_monsters", "Players cannot set off while monsters are nearby, using the same rule as sleeping in a bed.").define("blockNearMonsters", DEFAULT_BLOCK_TRAVEL_NEAR_MONSTERS);
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

	private static ModConfigSpec.Builder fastTravelOption(ModConfigSpec.Builder builder, String key, String comment) {
		return builder.comment(comment).translation("witchercraft.configuration.fast_travel." + key);
	}

	/** Empty, or a Minecraft language code such as {@code en_us} or {@code pl_pl}. */
	private static boolean validLanguageCode(Object value) {
		return value instanceof String code && (code.isEmpty() || code.length() <= 16 && code.matches("[a-z]{2,3}_[a-z0-9]{2,8}"));
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
	/** Requires the map and POIs too: sign markers are POIs, and travel is chosen on the map. */
	public static boolean fastTravelEnabled() { return poisEnabled() && (!loaded() ? DEFAULT_FAST_TRAVEL_ENABLED : FAST_TRAVEL_ENABLED.getAsBoolean()); }
	public static boolean signDropsItem() { return loaded() ? SIGN_DROPS_ITEM.getAsBoolean() : DEFAULT_SIGN_DROPS_ITEM; }
	public static int playerSignLimit() { return loaded() ? PLAYER_SIGN_LIMIT.getAsInt() : DEFAULT_PLAYER_SIGN_LIMIT; }
	public static boolean freeTravel() { return loaded() ? FREE_TRAVEL.getAsBoolean() : DEFAULT_FREE_TRAVEL; }
	public static int baseXpCost() { return loaded() ? BASE_XP_COST.getAsInt() : DEFAULT_BASE_XP_COST; }
	public static int blocksPerXp() { return Math.max(1, loaded() ? BLOCKS_PER_XP.getAsInt() : DEFAULT_BLOCKS_PER_XP); }
	public static int maximumXpCost() { return loaded() ? MAXIMUM_XP_COST.getAsInt() : DEFAULT_MAXIMUM_XP_COST; }
	public static boolean blockTravelInCombat() { return loaded() ? BLOCK_TRAVEL_IN_COMBAT.getAsBoolean() : DEFAULT_BLOCK_TRAVEL_IN_COMBAT; }
	public static boolean blockTravelNearMonsters() { return loaded() ? BLOCK_TRAVEL_NEAR_MONSTERS.getAsBoolean() : DEFAULT_BLOCK_TRAVEL_NEAR_MONSTERS; }
	public static boolean sharedSignDiscovery() { return loaded() ? SHARED_SIGN_DISCOVERY.getAsBoolean() : DEFAULT_SHARED_SIGN_DISCOVERY; }
	/** Empty means each player's own game language. Synced to clients with the rest of the server config. */
	public static String mapNameLanguage() {
		String code = loaded() ? MAP_NAME_LANGUAGE.get() : DEFAULT_MAP_NAME_LANGUAGE;
		return validLanguageCode(code) ? code : DEFAULT_MAP_NAME_LANGUAGE;
	}
}

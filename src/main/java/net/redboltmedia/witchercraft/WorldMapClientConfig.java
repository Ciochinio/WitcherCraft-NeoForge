package net.redboltmedia.witchercraft;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import net.minecraft.resources.Identifier;

import java.util.List;

/** Client-owned world-map visual settings. */
public final class WorldMapClientConfig {
	private static final ModConfigSpec SPEC;
	private static final ModConfigSpec.BooleanValue SHOW_DECORATIONS;
	private static final ModConfigSpec.DoubleValue TERRAIN_BRIGHTNESS;
	private static final ModConfigSpec.DoubleValue BIOME_COLOR_STRENGTH;
	private static final ModConfigSpec.DoubleValue HILLSHADE_STRENGTH;
	private static final ModConfigSpec.DoubleValue HILLSHADE_SLOPE_SENSITIVITY;
	private static final ModConfigSpec.DoubleValue CANOPY_RELIEF_STRENGTH;
	private static final ModConfigSpec.DoubleValue CANOPY_SHADOW_STRENGTH;
	private static final ModConfigSpec.DoubleValue FOLIAGE_OPACITY_SCALE;
	private static final ModConfigSpec.DoubleValue DECORATION_OPACITY_SCALE;
	private static final ModConfigSpec.DoubleValue MARKER_SCALE;
	private static final ModConfigSpec.DoubleValue ZOOM_SENSITIVITY;
	private static final ModConfigSpec.BooleanValue RESTORE_PREVIOUS_VIEW;
	private static final ModConfigSpec.BooleanValue DISCOVERY_ACTION_BAR;
	private static final ModConfigSpec.BooleanValue DEFAULT_PERSONAL_WAYPOINTS_VISIBLE;
	private static final ModConfigSpec.BooleanValue DEFAULT_UNKNOWN_POIS_VISIBLE;
	private static final ModConfigSpec.BooleanValue DEFAULT_DISCOVERED_POIS_VISIBLE;
	private static final ModConfigSpec.ConfigValue<List<? extends String>> DEFAULT_HIDDEN_POI_CATEGORIES;
	private static final ModConfigSpec.BooleanValue MINIMAP_ENABLED;
	private static final ModConfigSpec.EnumValue<MinimapCorner> MINIMAP_CORNER;
	private static final ModConfigSpec.BooleanValue MINIMAP_ROTATION;
	private static final ModConfigSpec.DoubleValue MINIMAP_ZOOM;
	private static final ModConfigSpec.IntValue MINIMAP_SIZE;
	private static final ModConfigSpec.IntValue MINIMAP_VIEWPORT_INSET;
	private static final ModConfigSpec.DoubleValue MINIMAP_TARGET_ARRIVAL_RADIUS;

	public enum MinimapCorner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		builder.comment("Personal presentation and interaction settings for the WitcherCraft world map.")
			.translation("witchercraft.configuration.world_map").push("worldMap");
		SHOW_DECORATIONS = option(builder, "show_decorations", "Draw flowers, grass, and other small decorative blocks on the world map.").define("showDecorations", true);
		TERRAIN_BRIGHTNESS = option(builder, "terrain_brightness", "Overall world-map terrain brightness.").defineInRange("terrainBrightness", 1.0, 0.5, 1.5);
		BIOME_COLOR_STRENGTH = option(builder, "biome_color_strength", "Strength of biome grass and foliage colors.").defineInRange("biomeColorStrength", 0.9, 0.0, 1.0);
		HILLSHADE_STRENGTH = option(builder, "hillshade_strength", "Brightness contrast applied by world-map terrain slope shading.").defineInRange("hillshadeStrength", 0.75, 0.0, 1.5);
		HILLSHADE_SLOPE_SENSITIVITY = option(builder, "hillshade_slope_sensitivity", "Sensitivity of world-map terrain shading to height differences between neighboring blocks.").defineInRange("hillshadeSlopeSensitivity", 1.0, 0.25, 4.0);
		CANOPY_RELIEF_STRENGTH = option(builder, "canopy_relief_strength", "Multiplier for slope contrast within and along raised foliage on the world map.").defineInRange("canopyReliefStrength", 1.35, 0.0, 3.0);
		CANOPY_SHADOW_STRENGTH = option(builder, "canopy_shadow_strength", "Maximum contact-shadow darkness beside raised foliage on the world map.").defineInRange("canopyShadowStrength", 0.35, 0.0, 0.6);
		FOLIAGE_OPACITY_SCALE = option(builder, "foliage_opacity_scale", "Multiplier for texture-derived foliage coverage on the world map.").defineInRange("foliageOpacityScale", 1.0, 0.0, 2.0);
		DECORATION_OPACITY_SCALE = option(builder, "decoration_opacity_scale", "Multiplier for texture-derived flower, grass, and decoration coverage on the world map.").defineInRange("decorationOpacityScale", 1.0, 0.0, 2.0);
		MARKER_SCALE = option(builder, "marker_scale", "Screen-size multiplier for world-map player, waypoint, and POI markers.").defineInRange("markerScale", 1.0, 0.5, 2.0);
		ZOOM_SENSITIVITY = option(builder, "zoom_sensitivity", "Multiplier for mouse-wheel and zoom-button sensitivity.").defineInRange("zoomSensitivity", 1.0, 0.25, 4.0);
		RESTORE_PREVIOUS_VIEW = option(builder, "restore_previous_view", "Restore the previous pan and zoom when reopening the map instead of centering on the player.").define("restorePreviousView", false);
		DISCOVERY_ACTION_BAR = option(builder, "discovery_action_bar", "Show an action-bar message when a point of interest is discovered.").define("discoveryActionBar", true);
		DEFAULT_PERSONAL_WAYPOINTS_VISIBLE = option(builder, "default_personal_waypoints_visible", "Show personal waypoints before a world-specific filter preference is saved.").define("defaultPersonalWaypointsVisible", true);
		DEFAULT_UNKNOWN_POIS_VISIBLE = option(builder, "default_unknown_pois_visible", "Show unknown POIs before a world-specific filter preference is saved.").define("defaultUnknownPoisVisible", true);
		DEFAULT_DISCOVERED_POIS_VISIBLE = option(builder, "default_discovered_pois_visible", "Show discovered POIs before a world-specific filter preference is saved.").define("defaultDiscoveredPoisVisible", true);
		DEFAULT_HIDDEN_POI_CATEGORIES = option(builder, "default_hidden_poi_categories", "POI categories hidden before a world-specific category filter preference is saved.")
			.defineListAllowEmpty("defaultHiddenPoiCategories", List.of("witchercraft:services"), () -> "witchercraft:services", WorldMapClientConfig::validIdentifier);
		builder.pop();
		builder.comment("Personal presentation settings for the WitcherCraft minimap.")
			.translation("witchercraft.configuration.minimap").push("minimap");
		MINIMAP_ENABLED = minimapOption(builder, "enabled", "Show the WitcherCraft minimap when the server permits it.").define("enabled", true);
		MINIMAP_CORNER = minimapOption(builder, "corner", "Screen corner occupied by the minimap.").defineEnum("corner", MinimapCorner.TOP_RIGHT);
		MINIMAP_ROTATION = minimapOption(builder, "rotation", "Rotate the minimap so the player's facing direction remains at the top. When disabled, north remains at the top.").define("rotation", true);
		MINIMAP_ZOOM = minimapOption(builder, "zoom", "Minimap screen pixels per world block. Larger values show a closer view.").defineInRange("zoom", 1.0, 0.25, 4.0);
		MINIMAP_SIZE = minimapOption(builder, "size", "Minimap diameter or side length in GUI pixels.").defineInRange("size", 128, 64, 256);
		MINIMAP_VIEWPORT_INSET = minimapOption(builder, "viewport_inset", "Inset of the terrain viewport from each edge, measured in texels of the 64 by 64 frame texture.").defineInRange("viewportInset", 5, 0, 24);
		MINIMAP_TARGET_ARRIVAL_RADIUS = minimapOption(builder, "target_arrival_radius", "Clear the active navigation target when you arrive within this many blocks of the destination. Set to 0 to disable automatic completion.")
			.defineInRange("targetArrivalRadius", 15.0, 0.0, 64.0);
		builder.pop();
		SPEC = builder.build();
	}

	private WorldMapClientConfig() {}
	private static ModConfigSpec.Builder option(ModConfigSpec.Builder builder, String key, String comment) {
		return builder.comment(comment).translation("witchercraft.configuration.world_map." + key);
	}
	private static ModConfigSpec.Builder minimapOption(ModConfigSpec.Builder builder, String key, String comment) {
		return builder.comment(comment).translation("witchercraft.configuration.minimap." + key);
	}
	public static void register() {
		ModContainer container = ModList.get().getModContainerById(WitchercraftMod.MODID)
			.orElseThrow(() -> new IllegalStateException("WitcherCraft mod container is unavailable during client-config registration"));
		container.registerConfig(ModConfig.Type.CLIENT, SPEC);
	}
	public static boolean showDecorations() { return SHOW_DECORATIONS.getAsBoolean(); }
	public static double terrainBrightness() { return TERRAIN_BRIGHTNESS.getAsDouble(); }
	public static double biomeColorStrength() { return BIOME_COLOR_STRENGTH.getAsDouble(); }
	public static double hillshadeStrength() { return HILLSHADE_STRENGTH.getAsDouble(); }
	public static double hillshadeSlopeSensitivity() { return HILLSHADE_SLOPE_SENSITIVITY.getAsDouble(); }
	public static double canopyReliefStrength() { return CANOPY_RELIEF_STRENGTH.getAsDouble(); }
	public static double canopyShadowStrength() { return CANOPY_SHADOW_STRENGTH.getAsDouble(); }
	public static double foliageOpacityScale() { return FOLIAGE_OPACITY_SCALE.getAsDouble(); }
	public static double decorationOpacityScale() { return DECORATION_OPACITY_SCALE.getAsDouble(); }
	public static double markerScale() { return MARKER_SCALE.getAsDouble(); }
	public static double zoomSensitivity() { return ZOOM_SENSITIVITY.getAsDouble(); }
	public static boolean restorePreviousView() { return RESTORE_PREVIOUS_VIEW.getAsBoolean(); }
	public static boolean discoveryActionBar() { return DISCOVERY_ACTION_BAR.getAsBoolean(); }
	public static boolean defaultPersonalWaypointsVisible() { return DEFAULT_PERSONAL_WAYPOINTS_VISIBLE.getAsBoolean(); }
	public static boolean defaultUnknownPoisVisible() { return DEFAULT_UNKNOWN_POIS_VISIBLE.getAsBoolean(); }
	public static boolean defaultDiscoveredPoisVisible() { return DEFAULT_DISCOVERED_POIS_VISIBLE.getAsBoolean(); }
	public static boolean defaultPoiCategoryVisible(Identifier category) {
		String id = category.toString();
		return DEFAULT_HIDDEN_POI_CATEGORIES.get().stream().noneMatch(id::equals);
	}
	public static boolean minimapEnabled() { return MINIMAP_ENABLED.getAsBoolean(); }
	public static MinimapCorner minimapCorner() { return MINIMAP_CORNER.get(); }
	public static boolean minimapRotation() { return MINIMAP_ROTATION.getAsBoolean(); }
	public static double minimapZoom() { return MINIMAP_ZOOM.getAsDouble(); }
	public static int minimapSize() { return MINIMAP_SIZE.getAsInt(); }
	public static int minimapViewportInset() { return MINIMAP_VIEWPORT_INSET.getAsInt(); }
	public static double minimapTargetArrivalRadius() { return MINIMAP_TARGET_ARRIVAL_RADIUS.getAsDouble(); }
	private static boolean validIdentifier(Object value) {
		return value instanceof String string && Identifier.tryParse(string) != null;
	}
}

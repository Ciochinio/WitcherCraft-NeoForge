package net.redboltmedia.witchercraft;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

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

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		builder.push("worldMap");
		SHOW_DECORATIONS = builder.comment("Draw flowers, grass, and other small decorative blocks on the world map.").define("showDecorations", true);
		TERRAIN_BRIGHTNESS = builder.comment("Overall world-map terrain brightness.").defineInRange("terrainBrightness", 1.0, 0.5, 1.5);
		BIOME_COLOR_STRENGTH = builder.comment("Strength of biome grass and foliage colors.").defineInRange("biomeColorStrength", 0.9, 0.0, 1.0);
		HILLSHADE_STRENGTH = builder.comment("Brightness contrast applied by world-map terrain slope shading.").defineInRange("hillshadeStrength", 0.75, 0.0, 1.5);
		HILLSHADE_SLOPE_SENSITIVITY = builder.comment("Sensitivity of world-map terrain shading to height differences between neighboring blocks.").defineInRange("hillshadeSlopeSensitivity", 1.0, 0.25, 4.0);
		CANOPY_RELIEF_STRENGTH = builder.comment("Multiplier for slope contrast within and along raised foliage on the world map.").defineInRange("canopyReliefStrength", 1.35, 0.0, 3.0);
		CANOPY_SHADOW_STRENGTH = builder.comment("Maximum contact-shadow darkness beside raised foliage on the world map.").defineInRange("canopyShadowStrength", 0.35, 0.0, 0.6);
		builder.pop();
		SPEC = builder.build();
	}

	private WorldMapClientConfig() {}
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
}

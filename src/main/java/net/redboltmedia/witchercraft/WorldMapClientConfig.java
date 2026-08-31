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

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		builder.push("worldMap");
		SHOW_DECORATIONS = builder.comment("Draw flowers, grass, and other small decorative blocks on the world map.").define("showDecorations", true);
		TERRAIN_BRIGHTNESS = builder.comment("Overall world-map terrain brightness.").defineInRange("terrainBrightness", 1.0, 0.5, 1.5);
		BIOME_COLOR_STRENGTH = builder.comment("Strength of biome grass and foliage colors.").defineInRange("biomeColorStrength", 0.9, 0.0, 1.0);
		HILLSHADE_STRENGTH = builder.comment("Strength of terrain slope and local-relief shading.").defineInRange("hillshadeStrength", 0.75, 0.0, 1.5);
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
}

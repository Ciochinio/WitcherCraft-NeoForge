package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Immutable, datapack-loaded description of one kind of world-map POI. */
public record WorldMapPoiDefinition(Identifier id, WorldMapPoiProviders.Binding<?> provider, String translationKey,
	String descriptionTranslationKey, Identifier category, Identifier icon, double revealRadius, double discoveryRadius, double uncertaintyRadius,
	boolean defaultVisible, double minimumZoom, List<Identifier> capabilities) {
	public static final int MAX_DEFINITIONS = 4096;
	public static final int MAX_CAPABILITIES = 32;
	public static final double MAX_RADIUS = 8192.0;
	public static final double MINIMUM_MAP_ZOOM = 0.25;
	public static final double MAXIMUM_MAP_ZOOM = 16.0;

	private static final Identifier DEFAULT_CATEGORY = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "general");
	private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "textures/screens/map_poi_default.png");

	/**
	 * File payload codec. The definition ID is deliberately supplied from the resource path,
	 * rather than duplicated inside the JSON file.
	 */
	public static final Codec<Template> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.PASSTHROUGH.fieldOf("provider").forGetter(Template::provider),
		Codec.STRING.optionalFieldOf("translation_key", "").forGetter(Template::translationKey),
		Codec.STRING.optionalFieldOf("description_translation_key", "").forGetter(Template::descriptionTranslationKey),
		Codec.STRING.optionalFieldOf("category", DEFAULT_CATEGORY.toString()).forGetter(Template::category),
		Codec.STRING.optionalFieldOf("icon", DEFAULT_ICON.toString()).forGetter(Template::icon),
		Codec.DOUBLE.optionalFieldOf("reveal_radius", 256.0).forGetter(Template::revealRadius),
		Codec.DOUBLE.optionalFieldOf("discovery_radius", 32.0).forGetter(Template::discoveryRadius),
		Codec.DOUBLE.optionalFieldOf("uncertainty_radius", 0.0).forGetter(Template::uncertaintyRadius),
		Codec.BOOL.optionalFieldOf("default_visible", true).forGetter(Template::defaultVisible),
		Codec.DOUBLE.optionalFieldOf("minimum_zoom", MINIMUM_MAP_ZOOM).forGetter(Template::minimumZoom),
		Codec.STRING.listOf().optionalFieldOf("capabilities", List.of()).forGetter(Template::capabilities)
	).apply(instance, Template::new));

	public WorldMapPoiDefinition {
		capabilities = List.copyOf(capabilities);
	}

	public static DataResult<WorldMapPoiDefinition> resolve(Identifier id, Template template, HolderLookup.Provider registries) {
		if (id == null)
			return error("Missing definition ID");
		if (!validTranslationKey(template.translationKey()) && !template.translationKey().isEmpty())
			return error("Invalid translation_key");
		if (!validTranslationKey(template.descriptionTranslationKey()) && !template.descriptionTranslationKey().isEmpty())
			return error("Invalid description_translation_key");
		Identifier category = Identifier.tryParse(template.category());
		if (category == null)
			return error("Invalid category identifier");
		Identifier icon = Identifier.tryParse(template.icon());
		if (icon == null)
			return error("Invalid icon identifier");
		if (!validRadius(template.revealRadius()) || !validRadius(template.discoveryRadius()) || !validRadius(template.uncertaintyRadius()))
			return error("Radii must be finite values from 0 through " + MAX_RADIUS);
		if (template.revealRadius() > 0.0 && template.discoveryRadius() > template.revealRadius())
			return error("discovery_radius may not exceed reveal_radius");
		if (!Double.isFinite(template.minimumZoom()) || template.minimumZoom() < MINIMUM_MAP_ZOOM || template.minimumZoom() > MAXIMUM_MAP_ZOOM)
			return error("minimum_zoom must be between " + MINIMUM_MAP_ZOOM + " and " + MAXIMUM_MAP_ZOOM);
		if (template.capabilities().size() > MAX_CAPABILITIES)
			return error("Too many capabilities (maximum " + MAX_CAPABILITIES + ")");

		List<Identifier> capabilities = new java.util.ArrayList<>(template.capabilities().size());
		Set<Identifier> uniqueCapabilities = new HashSet<>();
		for (String rawCapability : template.capabilities()) {
			Identifier capability = Identifier.tryParse(rawCapability);
			if (capability == null)
				return error("Invalid capability identifier: " + rawCapability);
			if (!uniqueCapabilities.add(capability))
				return error("Duplicate capability identifier: " + capability);
			capabilities.add(capability);
		}

		String translationKey = template.translationKey().isEmpty() ? derivedTranslationKey(id) : template.translationKey();
		return WorldMapPoiProviders.decodeAndValidate(template.provider(), registries).map(provider -> new WorldMapPoiDefinition(
			id, provider, translationKey, template.descriptionTranslationKey().isEmpty() ? translationKey + ".description" : template.descriptionTranslationKey(), category, icon,
			template.revealRadius(), template.discoveryRadius(), template.uncertaintyRadius(), template.defaultVisible(), template.minimumZoom(), capabilities));
	}

	private static String derivedTranslationKey(Identifier id) {
		return "poi." + id.getNamespace() + "." + id.getPath().replace('/', '.');
	}

	private static boolean validTranslationKey(String key) {
		return key != null && !key.isBlank() && key.length() <= 160 && key.codePoints().allMatch(character ->
			Character.isLetterOrDigit(character) || character == '.' || character == '_' || character == '-' || character == '/');
	}

	private static boolean validRadius(double radius) {
		return Double.isFinite(radius) && radius >= 0.0 && radius <= MAX_RADIUS;
	}

	private static <T> DataResult<T> error(String message) {
		return DataResult.error(() -> message);
	}

	public record Template(Dynamic<?> provider, String translationKey, String descriptionTranslationKey, String category, String icon, double revealRadius,
		double discoveryRadius, double uncertaintyRadius, boolean defaultVisible, double minimumZoom, List<String> capabilities) {
		public Template {
			capabilities = List.copyOf(capabilities);
		}
	}
}

package net.redboltmedia.witchercraft;

import net.minecraft.resources.Identifier;

import java.util.UUID;

/** Wire-safe client presentation of one authorized POI marker. */
public sealed interface WorldMapPoiMarker permits WorldMapPoiMarker.Unknown, WorldMapPoiMarker.Discovered {
	double MAX_ABSOLUTE_COORDINATE = 30_000_000.0 + WorldMapPoiDefinition.MAX_RADIUS;

	UUID markerId();

	double x();

	double z();

	double minimumZoom();

	boolean defaultVisible();

	record Unknown(UUID markerId, double x, double z, double minimumZoom, boolean defaultVisible) implements WorldMapPoiMarker {
		public Unknown {
			validateCommon(markerId, x, z, minimumZoom);
		}
	}

	/**
	 * {@code customName} is a server-approved plain-text name shown after the translated kind when not empty.
	 * {@code nameKey} is an optional translation key of a generated place name that takes precedence over it.
	 */
	record Discovered(UUID markerId, double x, double z, String translationKey, String descriptionTranslationKey, Identifier category, Identifier icon,
		double minimumZoom, boolean defaultVisible, String customName, String nameKey) implements WorldMapPoiMarker {
		public Discovered {
			validateCommon(markerId, x, z, minimumZoom);
			if (translationKey == null || translationKey.isBlank() || translationKey.length() > 160 || descriptionTranslationKey == null
				|| descriptionTranslationKey.isBlank() || descriptionTranslationKey.length() > 160 || category == null || icon == null
				|| !WorldMapPoiInstance.validCustomName(customName) || !WorldMapPoiInstance.validNameKey(nameKey))
				throw new IllegalArgumentException("Invalid discovered POI presentation");
		}
	}

	/**
	 * Client-side presentation name of a POI: the translated kind alone, or "Kind: name" when the POI
	 * has a name (for example "Signpost: Kaer Morhen road"). A generated {@code nameKey} is translated in
	 * the world's map name language, falling back to {@code customName}.
	 */
	static net.minecraft.network.chat.Component displayName(net.minecraft.network.chat.Component kind, String nameKey, String customName) {
		String name = WorldMapPlaceNames.resolve(nameKey, customName);
		if (name.isEmpty())
			return kind;
		return net.minecraft.network.chat.Component.translatableWithFallback("gui.witchercraft.map.poi.named", "%s: %s", kind,
			net.minecraft.network.chat.Component.literal(name));
	}

	private static void validateCommon(UUID markerId, double x, double z, double minimumZoom) {
		if (markerId == null || !Double.isFinite(x) || !Double.isFinite(z) || Math.abs(x) > MAX_ABSOLUTE_COORDINATE
			|| Math.abs(z) > MAX_ABSOLUTE_COORDINATE || !Double.isFinite(minimumZoom)
			|| minimumZoom < WorldMapPoiDefinition.MINIMUM_MAP_ZOOM || minimumZoom > WorldMapPoiDefinition.MAXIMUM_MAP_ZOOM)
			throw new IllegalArgumentException("Invalid world-map POI marker");
	}
}

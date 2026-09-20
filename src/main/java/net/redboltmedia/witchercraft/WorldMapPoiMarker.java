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

	record Discovered(UUID markerId, double x, double z, String translationKey, Identifier category, Identifier icon,
		double minimumZoom, boolean defaultVisible) implements WorldMapPoiMarker {
		public Discovered {
			validateCommon(markerId, x, z, minimumZoom);
			if (translationKey == null || translationKey.isBlank() || translationKey.length() > 160 || category == null || icon == null)
				throw new IllegalArgumentException("Invalid discovered POI presentation");
		}
	}

	private static void validateCommon(UUID markerId, double x, double z, double minimumZoom) {
		if (markerId == null || !Double.isFinite(x) || !Double.isFinite(z) || Math.abs(x) > MAX_ABSOLUTE_COORDINATE
			|| Math.abs(z) > MAX_ABSOLUTE_COORDINATE || !Double.isFinite(minimumZoom)
			|| minimumZoom < WorldMapPoiDefinition.MINIMUM_MAP_ZOOM || minimumZoom > WorldMapPoiDefinition.MAXIMUM_MAP_ZOOM)
			throw new IllegalArgumentException("Invalid world-map POI marker");
	}
}

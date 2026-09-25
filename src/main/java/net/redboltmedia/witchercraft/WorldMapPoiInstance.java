package net.redboltmedia.witchercraft;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * One provider-observed POI location. {@code customName} is a server-validated plain-text name
 * for lifecycle-managed POIs such as fast-travel signs; it is empty for data-driven kinds.
 */
public record WorldMapPoiInstance(UUID markerId, Identifier definitionId, Identifier providerType, Identifier sourceId,
	String providerIdentity, Identifier dimension, BlockPos anchor, boolean active, String customName) {
	private static final String IDENTITY_PREFIX = "witchercraft:world_map_poi:v1|";
	public static final int MAX_CUSTOM_NAME_CHARACTERS = 64;

	public WorldMapPoiInstance {
		if (markerId == null || definitionId == null || providerType == null || sourceId == null || providerIdentity == null || dimension == null || anchor == null
			|| customName == null)
			throw new IllegalArgumentException("A POI instance may not contain null fields");
		if (!validCustomName(customName))
			throw new IllegalArgumentException("Invalid POI custom name");
	}

	public static WorldMapPoiInstance observed(Identifier definitionId, Identifier providerType, Identifier sourceId,
		String providerIdentity, Identifier dimension, BlockPos anchor) {
		UUID markerId = markerIdForIdentity(providerIdentity);
		return new WorldMapPoiInstance(markerId, definitionId, providerType, sourceId, providerIdentity, dimension, anchor.immutable(), true, "");
	}

	public static UUID markerIdForIdentity(String providerIdentity) {
		return UUID.nameUUIDFromBytes((IDENTITY_PREFIX + providerIdentity).getBytes(StandardCharsets.UTF_8));
	}

	/** Empty, or 1 to 64 code points of plain text without control characters or surrounding whitespace. */
	public static boolean validCustomName(String name) {
		if (name == null)
			return false;
		if (name.isEmpty())
			return true;
		return name.strip().equals(name) && name.codePointCount(0, name.length()) <= MAX_CUSTOM_NAME_CHARACTERS
			&& name.codePoints().noneMatch(Character::isISOControl);
	}

	public WorldMapPoiInstance withActive(boolean value) {
		return active == value ? this : new WorldMapPoiInstance(markerId, definitionId, providerType, sourceId, providerIdentity, dimension, anchor, value, customName);
	}

	public WorldMapPoiInstance withCustomName(String value) {
		return customName.equals(value) ? this : new WorldMapPoiInstance(markerId, definitionId, providerType, sourceId, providerIdentity, dimension, anchor, active, value);
	}
}

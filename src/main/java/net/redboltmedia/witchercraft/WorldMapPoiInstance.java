package net.redboltmedia.witchercraft;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** One provider-observed POI location. Persistence is added in Batch 4C. */
public record WorldMapPoiInstance(UUID markerId, Identifier definitionId, Identifier providerType, Identifier sourceId,
	String providerIdentity, Identifier dimension, BlockPos anchor, boolean active) {
	private static final String IDENTITY_PREFIX = "witchercraft:world_map_poi:v1|";

	public WorldMapPoiInstance {
		if (markerId == null || definitionId == null || providerType == null || sourceId == null || providerIdentity == null || dimension == null || anchor == null)
			throw new IllegalArgumentException("A POI instance may not contain null fields");
	}

	public static WorldMapPoiInstance observed(Identifier definitionId, Identifier providerType, Identifier sourceId,
		String providerIdentity, Identifier dimension, BlockPos anchor) {
		UUID markerId = UUID.nameUUIDFromBytes((IDENTITY_PREFIX + providerIdentity).getBytes(StandardCharsets.UTF_8));
		return new WorldMapPoiInstance(markerId, definitionId, providerType, sourceId, providerIdentity, dimension, anchor.immutable(), true);
	}

	public WorldMapPoiInstance withActive(boolean value) {
		return active == value ? this : new WorldMapPoiInstance(markerId, definitionId, providerType, sourceId, providerIdentity, dimension, anchor, value);
	}
}

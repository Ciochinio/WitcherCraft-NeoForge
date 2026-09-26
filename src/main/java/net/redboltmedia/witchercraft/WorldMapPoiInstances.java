package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-owned persistent registry of provider-observed POI instances.
 * Version 2 adds the optional {@code custom_name} used by lifecycle-managed POIs; version 3 adds the optional
 * {@code name_key} of generated place names. Older data loads unchanged.
 */
public final class WorldMapPoiInstances extends SavedData {
	public static final int FORMAT_VERSION = 3;
	private static final int OLDEST_READABLE_VERSION = 1;
	public static final int MAX_INSTANCES = 262_144;
	private static final int MAX_IDENTITY_LENGTH = 512;
	private static final int MAX_ABSOLUTE_COORDINATE = 30_000_000;

	private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map/poi_instances");
	private static final Codec<WorldMapPoiInstances> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("format_version", FORMAT_VERSION).forGetter(ignored -> FORMAT_VERSION),
		StoredInstance.CODEC.listOf().optionalFieldOf("instances", List.of()).forGetter(WorldMapPoiInstances::storedInstances)
	).apply(instance, WorldMapPoiInstances::new));
	public static final SavedDataType<WorldMapPoiInstances> TYPE = new SavedDataType<>(DATA_ID, WorldMapPoiInstances::new, CODEC);

	private final Map<UUID, WorldMapPoiInstance> instances = new LinkedHashMap<>();

	public WorldMapPoiInstances() {
	}

	private WorldMapPoiInstances(int formatVersion, List<StoredInstance> stored) {
		if (formatVersion < OLDEST_READABLE_VERSION || formatVersion > FORMAT_VERSION)
			WitchercraftMod.LOGGER.warn("Loading POI instance data version {} with reader version {}", formatVersion, FORMAT_VERSION);
		for (StoredInstance entry : stored) {
			if (instances.size() >= MAX_INSTANCES) {
				WitchercraftMod.LOGGER.warn("Discarded POI instances beyond the limit of {}", MAX_INSTANCES);
				break;
			}
			WorldMapPoiInstance decoded = entry.decode();
			if (decoded == null || instances.putIfAbsent(decoded.markerId(), decoded) != null)
				WitchercraftMod.LOGGER.warn("Discarded invalid or duplicate world-map POI instance");
		}
	}

	public static WorldMapPoiInstances get(MinecraftServer server) {
		requireServerThread(server);
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public Collection<WorldMapPoiInstance> values() {
		return List.copyOf(instances.values());
	}

	public @Nullable WorldMapPoiInstance get(UUID markerId) {
		return instances.get(markerId);
	}

	public ObservationResult observe(WorldMapPoiInstance observed) {
		if (instances.size() >= MAX_INSTANCES && !instances.containsKey(observed.markerId()))
			return ObservationResult.LIMIT_REACHED;
		WorldMapPoiInstance previous = instances.get(observed.markerId());
		if (previous != null && !previous.providerIdentity().equals(observed.providerIdentity()))
			return ObservationResult.COLLISION;
		if (previous == null) {
			instances.put(observed.markerId(), observed);
			setDirty();
			return ObservationResult.CREATED;
		}
		if (!previous.equals(observed)) {
			instances.put(observed.markerId(), observed);
			setDirty();
			return ObservationResult.UPDATED;
		}
		return ObservationResult.UNCHANGED;
	}

	public boolean setActive(UUID markerId, boolean active) {
		WorldMapPoiInstance previous = instances.get(markerId);
		if (previous == null || previous.active() == active)
			return false;
		instances.put(markerId, previous.withActive(active));
		setDirty();
		return true;
	}

	/** Deletes a record. Only lifecycle-managed POIs whose world object was destroyed are removed this way. */
	public @Nullable WorldMapPoiInstance remove(UUID markerId) {
		WorldMapPoiInstance removed = instances.remove(markerId);
		if (removed != null)
			setDirty();
		return removed;
	}

	public boolean setCustomName(UUID markerId, String customName) {
		WorldMapPoiInstance previous = instances.get(markerId);
		if (previous == null || !WorldMapPoiInstance.validCustomName(customName) || previous.customName().equals(customName))
			return false;
		instances.put(markerId, previous.withCustomName(customName));
		setDirty();
		return true;
	}

	private List<StoredInstance> storedInstances() {
		return instances.values().stream().sorted(Comparator.comparing(value -> value.markerId().toString())).map(StoredInstance::from).toList();
	}

	private static void requireServerThread(MinecraftServer server) {
		if (server == null || !server.isSameThread())
			throw new IllegalStateException("World-map POI instances may only be accessed on the server thread");
	}

	public enum ObservationResult {
		CREATED, UPDATED, UNCHANGED, COLLISION, LIMIT_REACHED
	}

	private record StoredInstance(String markerId, String definitionId, String providerType, String sourceId,
		String providerIdentity, String dimension, int anchorX, int anchorY, int anchorZ, boolean active, String customName, String nameKey) {
		private static final Codec<StoredInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("marker_id", "").forGetter(StoredInstance::markerId),
			Codec.STRING.optionalFieldOf("definition_id", "").forGetter(StoredInstance::definitionId),
			Codec.STRING.optionalFieldOf("provider_type", "").forGetter(StoredInstance::providerType),
			Codec.STRING.optionalFieldOf("source_id", "").forGetter(StoredInstance::sourceId),
			Codec.STRING.optionalFieldOf("provider_identity", "").forGetter(StoredInstance::providerIdentity),
			Codec.STRING.optionalFieldOf("dimension", "").forGetter(StoredInstance::dimension),
			Codec.INT.optionalFieldOf("anchor_x", 0).forGetter(StoredInstance::anchorX),
			Codec.INT.optionalFieldOf("anchor_y", 0).forGetter(StoredInstance::anchorY),
			Codec.INT.optionalFieldOf("anchor_z", 0).forGetter(StoredInstance::anchorZ),
			Codec.BOOL.optionalFieldOf("active", false).forGetter(StoredInstance::active),
			Codec.STRING.optionalFieldOf("custom_name", "").forGetter(StoredInstance::customName),
			Codec.STRING.optionalFieldOf("name_key", "").forGetter(StoredInstance::nameKey)
		).apply(instance, StoredInstance::new));

		private static StoredInstance from(WorldMapPoiInstance value) {
			return new StoredInstance(value.markerId().toString(), value.definitionId().toString(), value.providerType().toString(),
				value.sourceId().toString(), value.providerIdentity(), value.dimension().toString(), value.anchor().getX(),
				value.anchor().getY(), value.anchor().getZ(), value.active(), value.customName(), value.nameKey());
		}

		private @Nullable WorldMapPoiInstance decode() {
			try {
				UUID parsedMarkerId = UUID.fromString(markerId);
				Identifier parsedDefinitionId = Identifier.tryParse(definitionId);
				Identifier parsedProviderType = Identifier.tryParse(providerType);
				Identifier parsedSourceId = Identifier.tryParse(sourceId);
				Identifier parsedDimension = Identifier.tryParse(dimension);
				if (parsedDefinitionId == null || parsedProviderType == null || parsedSourceId == null || parsedDimension == null
					|| providerIdentity.isBlank() || providerIdentity.length() > MAX_IDENTITY_LENGTH
					|| Math.abs((long) anchorX) > MAX_ABSOLUTE_COORDINATE || Math.abs((long) anchorY) > MAX_ABSOLUTE_COORDINATE
					|| Math.abs((long) anchorZ) > MAX_ABSOLUTE_COORDINATE || !WorldMapPoiInstance.validCustomName(customName)
					|| !WorldMapPoiInstance.validNameKey(nameKey) || !parsedMarkerId.equals(WorldMapPoiInstance.markerIdForIdentity(providerIdentity)))
					return null;
				return new WorldMapPoiInstance(parsedMarkerId, parsedDefinitionId, parsedProviderType, parsedSourceId,
					providerIdentity, parsedDimension, new BlockPos(anchorX, anchorY, anchorZ), active, customName, nameKey);
			} catch (IllegalArgumentException exception) {
				return null;
			}
		}
	}
}

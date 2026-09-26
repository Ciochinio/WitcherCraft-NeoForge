package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Connection- and world-scoped cache of server-authorized POI presentation. */
public final class WorldMapPoiClientCache {
	private static final UUID NO_WORLD = new UUID(0L, 0L);
	private static final int MAX_CACHED_CELLS_PER_DIMENSION = 4096;
	private static final int MAX_BUFFERED_MARKERS = 4096;
	private static final long REQUEST_RETRY_NANOS = 250_000_000L;

	private static final Map<Identifier, DimensionCache> DIMENSIONS = new HashMap<>();
	private static Object connectionIdentity;
	private static UUID worldId = NO_WORLD;
	private static long definitionGeneration;
	private static int nextRequestId = 1;
	private static PendingRequest inFlight;
	private static long nextRequestNanos;

	private WorldMapPoiClientCache() {
	}

	/** Called while the map renders; requests only cells not already authoritative. */
	public static void updateView(Identifier dimension, int viewportWidth, int viewportHeight, double centerX, double centerZ, double zoom) {
		ensureConnection();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.getConnection() == null || minecraft.player == null || dimension == null || viewportWidth <= 0 || viewportHeight <= 0
			|| !Double.isFinite(centerX) || !Double.isFinite(centerZ) || !Double.isFinite(zoom) || zoom <= 0.0 || inFlight != null
			|| System.nanoTime() < nextRequestNanos)
			return;
		DimensionCache cache = DIMENSIONS.computeIfAbsent(dimension, ignored -> new DimensionCache());
		int minCellX = WorldMapPoiSpatialIndex.cellFor(centerX - viewportWidth / (2.0 * zoom));
		int maxCellX = WorldMapPoiSpatialIndex.cellFor(centerX + viewportWidth / (2.0 * zoom));
		int minCellZ = WorldMapPoiSpatialIndex.cellFor(centerZ - viewportHeight / (2.0 * zoom));
		int maxCellZ = WorldMapPoiSpatialIndex.cellFor(centerZ + viewportHeight / (2.0 * zoom));
		List<CellDistance> missing = new ArrayList<>();
		for (int z = minCellZ; z <= maxCellZ; z++)
			for (int x = minCellX; x <= maxCellX; x++) {
				long packed = WorldMapPoiSpatialIndex.packCell(x, z);
				if (!WorldMapPoiSpatialIndex.validCell(packed) || cache.validatedCells.contains(packed))
					continue;
				double dx = (x + 0.5) * WorldMapPoiSpatialIndex.CELL_SIZE - centerX;
				double dz = (z + 0.5) * WorldMapPoiSpatialIndex.CELL_SIZE - centerZ;
				missing.add(new CellDistance(packed, dx * dx + dz * dz));
			}
		if (missing.isEmpty())
			return;
		missing.sort(Comparator.comparingDouble(CellDistance::distanceSquared));
		int count = Math.min(missing.size(), WorldMapPoiViewRequestMessage.MAX_CELLS);
		long[] cells = new long[count];
		for (int index = 0; index < count; index++)
			cells[index] = missing.get(index).cell();
		int requestId = nextRequestId++;
		if (nextRequestId <= 0)
			nextRequestId = 1;
		inFlight = new PendingRequest(requestId, dimension, Set.copyOf(toSet(cells)), new LinkedHashMap<>());
		ClientPacketDistributor.sendToServer(new WorldMapPoiViewRequestMessage(requestId, dimension, definitionGeneration, cells));
	}

	/** Read-only Stage 4E input. */
	public static List<WorldMapPoiMarker> markers(Identifier dimension) {
		ensureConnection();
		DimensionCache cache = DIMENSIONS.get(dimension);
		return cache == null ? List.of() : List.copyOf(cache.markers.values());
	}

	/** Server-issued identity used to isolate client-only presentation preferences. */
	public static UUID worldId() {
		ensureConnection();
		return worldId;
	}

	static void acceptData(WorldMapPoiDataMessage message) {
		ensureConnection();
		if (Minecraft.getInstance().getConnection() == null)
			return;
		if (message.requestId() == 0) {
			if (message.definitionGeneration() < definitionGeneration)
				return;
			if (message.definitionGeneration() > definitionGeneration) {
				clearPresentation();
				definitionGeneration = message.definitionGeneration();
			}
			DimensionCache cache = DIMENSIONS.computeIfAbsent(message.dimension(), ignored -> new DimensionCache());
			for (WorldMapPoiMarker marker : message.markers())
				cache.upsert(marker);
			WitchercraftMod.LOGGER.info("World-map POI push accepted: dimension={}, markers={}, generation={}",
				message.dimension(), message.markers().size(), definitionGeneration);
			return;
		}
		if (inFlight == null || inFlight.requestId != message.requestId() || !inFlight.dimension.equals(message.dimension())
			|| message.definitionGeneration() != definitionGeneration)
			return;
		for (WorldMapPoiMarker marker : message.markers()) {
			long cell = presentationCell(marker);
			if (inFlight.cells.contains(cell) && inFlight.markers.size() < MAX_BUFFERED_MARKERS)
				inFlight.markers.put(marker.markerId(), marker);
		}
	}

	static void completeRequest(WorldMapPoiRequestCompleteMessage message) {
		ensureConnection();
		if (Minecraft.getInstance().getConnection() == null)
			return;
		if (!message.worldId().equals(worldId) || message.definitionGeneration() != definitionGeneration) {
			clearPresentation();
			worldId = message.worldId();
			definitionGeneration = message.definitionGeneration();
			nextRequestNanos = 0L;
			return;
		}
		PendingRequest pending = inFlight;
		if (pending == null || pending.requestId != message.requestId())
			return;
		Set<Long> completedCells = toSet(message.cells());
		if (message.accepted() && completedCells.equals(pending.cells)) {
			DimensionCache cache = DIMENSIONS.computeIfAbsent(pending.dimension, ignored -> new DimensionCache());
			cache.replaceCells(completedCells, pending.markers.values());
			WitchercraftMod.LOGGER.info("World-map POI cache updated: dimension={}, cells={}, markers={}, generation={}",
				pending.dimension, completedCells.size(), pending.markers.size(), definitionGeneration);
		}
		inFlight = null;
		nextRequestNanos = System.nanoTime() + REQUEST_RETRY_NANOS;
	}

	/** Drops one marker that the server deleted, such as a destroyed fast-travel sign. */
	static void acceptRemoval(Identifier dimension, UUID presentationId) {
		ensureConnection();
		DimensionCache cache = DIMENSIONS.get(dimension);
		if (cache != null)
			cache.removeMarker(presentationId);
	}

	public static void reset(UUID nextWorldId, long nextDefinitionGeneration) {
		ensureConnection();
		clearPresentation();
		WorldMapPlaceNames.clear();
		worldId = nextWorldId;
		definitionGeneration = nextDefinitionGeneration;
		WitchercraftMod.LOGGER.info("World-map POI cache reset: world={}, generation={}", worldId, definitionGeneration);
	}

	public static void clear() {
		clearPresentation();
		worldId = NO_WORLD;
		definitionGeneration = 0L;
		nextRequestId = 1;
	}

	private static void ensureConnection() {
		Object current = Minecraft.getInstance().getConnection();
		if (connectionIdentity != current) {
			clear();
			connectionIdentity = current;
		}
	}

	private static void clearPresentation() {
		DIMENSIONS.clear();
		inFlight = null;
		nextRequestNanos = 0L;
	}

	private static long presentationCell(WorldMapPoiMarker marker) {
		return WorldMapPoiSpatialIndex.packCell(WorldMapPoiSpatialIndex.cellFor(marker.x()), WorldMapPoiSpatialIndex.cellFor(marker.z()));
	}

	private static Set<Long> toSet(long[] values) {
		Set<Long> result = new LinkedHashSet<>();
		for (long value : values)
			result.add(value);
		return result;
	}

	private static final class DimensionCache {
		private final Map<UUID, WorldMapPoiMarker> markers = new LinkedHashMap<>();
		private final Map<UUID, Long> markerCells = new HashMap<>();
		private final Map<Long, Set<UUID>> markersByCell = new HashMap<>();
		private final Set<Long> validatedCells = new LinkedHashSet<>();

		private void replaceCells(Set<Long> cells, Iterable<WorldMapPoiMarker> replacements) {
			for (long cell : cells)
				removeCell(cell);
			for (WorldMapPoiMarker marker : replacements)
				upsert(marker);
			validatedCells.addAll(cells);
			trimCells();
		}

		private void upsert(WorldMapPoiMarker marker) {
			removeMarker(marker.markerId());
			long cell = presentationCell(marker);
			markers.put(marker.markerId(), marker);
			markerCells.put(marker.markerId(), cell);
			markersByCell.computeIfAbsent(cell, ignored -> new HashSet<>()).add(marker.markerId());
		}

		private void removeCell(long cell) {
			Set<UUID> ids = markersByCell.remove(cell);
			if (ids != null)
				for (UUID id : List.copyOf(ids)) {
					markers.remove(id);
					markerCells.remove(id);
				}
			validatedCells.remove(cell);
		}

		private void removeMarker(UUID markerId) {
			markers.remove(markerId);
			Long oldCell = markerCells.remove(markerId);
			if (oldCell == null)
				return;
			Set<UUID> ids = markersByCell.get(oldCell);
			if (ids != null) {
				ids.remove(markerId);
				if (ids.isEmpty())
					markersByCell.remove(oldCell);
			}
		}

		private void trimCells() {
			Iterator<Long> iterator = validatedCells.iterator();
			while (validatedCells.size() > MAX_CACHED_CELLS_PER_DIMENSION && iterator.hasNext()) {
				long cell = iterator.next();
				iterator.remove();
				Set<UUID> ids = markersByCell.remove(cell);
				if (ids != null)
					for (UUID id : ids) {
						markers.remove(id);
						markerCells.remove(id);
					}
			}
		}
	}

	private record PendingRequest(int requestId, Identifier dimension, Set<Long> cells, Map<UUID, WorldMapPoiMarker> markers) {
	}

	private record CellDistance(long cell, double distanceSquared) {
	}
}

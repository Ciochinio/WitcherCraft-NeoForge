package net.redboltmedia.witchercraft;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Runtime-only dimension and coarse-cell index for active POI instances. */
public final class WorldMapPoiSpatialIndex {
	private static final int CELL_SIZE = 256;

	private final Map<Identifier, Map<Long, Set<UUID>>> cellsByDimension = new HashMap<>();
	private final Map<UUID, IndexedLocation> locations = new HashMap<>();

	public void rebuild(Collection<WorldMapPoiInstance> instances) {
		cellsByDimension.clear();
		locations.clear();
		for (WorldMapPoiInstance instance : instances)
			if (instance.active())
				add(instance);
	}

	public void upsert(WorldMapPoiInstance instance) {
		remove(instance.markerId());
		if (instance.active())
			add(instance);
	}

	public List<UUID> query(Identifier dimension, double x, double z, double radius) {
		Map<Long, Set<UUID>> cells = cellsByDimension.get(dimension);
		if (cells == null || radius < 0.0 || !Double.isFinite(radius))
			return List.of();
		int minCellX = cell(x - radius);
		int maxCellX = cell(x + radius);
		int minCellZ = cell(z - radius);
		int maxCellZ = cell(z + radius);
		List<UUID> result = new ArrayList<>();
		for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++)
			for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
				Set<UUID> bucket = cells.get(pack(cellX, cellZ));
				if (bucket != null)
					result.addAll(bucket);
			}
		return result;
	}

	private void add(WorldMapPoiInstance instance) {
		int cellX = Math.floorDiv(instance.anchor().getX(), CELL_SIZE);
		int cellZ = Math.floorDiv(instance.anchor().getZ(), CELL_SIZE);
		long cell = pack(cellX, cellZ);
		cellsByDimension.computeIfAbsent(instance.dimension(), ignored -> new HashMap<>())
			.computeIfAbsent(cell, ignored -> new LinkedHashSet<>()).add(instance.markerId());
		locations.put(instance.markerId(), new IndexedLocation(instance.dimension(), cell));
	}

	private void remove(UUID markerId) {
		IndexedLocation location = locations.remove(markerId);
		if (location == null)
			return;
		Map<Long, Set<UUID>> cells = cellsByDimension.get(location.dimension());
		if (cells == null)
			return;
		Set<UUID> bucket = cells.get(location.cell());
		if (bucket != null) {
			bucket.remove(markerId);
			if (bucket.isEmpty())
				cells.remove(location.cell());
		}
		if (cells.isEmpty())
			cellsByDimension.remove(location.dimension());
	}

	private static int cell(double coordinate) {
		return (int) Math.floor(coordinate / CELL_SIZE);
	}

	private static long pack(int x, int z) {
		return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32);
	}

	private record IndexedLocation(Identifier dimension, long cell) {
	}
}

package net.redboltmedia.witchercraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). Places one fast-travel signpost next
 * to the town-center bell of every newly generated village.
 *
 * Minecraft has no "structure placed" event, so this follows chunk loads. The chunk load event may not
 * touch the level, so it only queues the village references of the chunk; the work runs on the next
 * server tick. A village becomes eligible when a chunk under its town-center piece (the first piece of
 * the structure start) is newly generated, so villages generated before this feature never get a sign.
 * The sign is placed once every chunk under the town center is loaded, which is checked without loading
 * anything. Every eligible village is decided exactly once and the decision is saved.
 *
 * Names come from translation keys {@code signpost_name.witchercraft.<kind>.<id>} in the mod's English
 * language file, where kind is the village type (plains, desert, savanna, snowy, taiga) or {@code other}.
 */
@EventBusSubscriber
public final class FastTravelVillageSigns {
	public static final String NAME_KEY_PREFIX = "signpost_name.witchercraft.";
	public static final String OTHER_KIND = "other";
	private static final int MAX_QUEUED_PER_TICK = 32;
	/** First search: columns within this horizontal distance of the anchor. */
	private static final int NEAR_RADIUS = 4;
	private static final int SEARCH_DOWN = 4;
	private static final int SEARCH_UP = 1;

	private static final ConcurrentLinkedQueue<LoadedChunk> QUEUE = new ConcurrentLinkedQueue<>();
	private static @Nullable Map<String, List<String>> nameLists;

	private FastTravelVillageSigns() {
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)
			|| !WorldMapServerConfig.fastTravelEnabled())
			return;
		// Reading the chunk's own data is safe here; the level itself may not be touched until next tick.
		Registry<Structure> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		List<VillageReference> villages = new ArrayList<>();
		for (Map.Entry<Structure, LongSet> entry : event.getChunk().getAllReferences().entrySet()) {
			if (entry.getValue().isEmpty() || !structures.wrapAsHolder(entry.getKey()).is(StructureTags.VILLAGE))
				continue;
			Identifier structureId = structures.getKey(entry.getKey());
			if (structureId != null)
				for (long start : entry.getValue())
					villages.add(new VillageReference(entry.getKey(), structureId, start));
		}
		if (!villages.isEmpty())
			QUEUE.add(new LoadedChunk(event.getChunk().getPos(), event.isNewChunk(), villages));
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (QUEUE.isEmpty())
			return;
		ServerLevel level = event.getServer().overworld();
		Data data = level.getServer().getDataStorage().computeIfAbsent(Data.TYPE);
		for (int processed = 0; processed < MAX_QUEUED_PER_TICK; processed++) {
			LoadedChunk chunk = QUEUE.poll();
			if (chunk == null)
				return;
			for (VillageReference village : chunk.villages())
				process(level, data, chunk, village);
		}
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		QUEUE.clear();
	}

	private static void process(ServerLevel level, Data data, LoadedChunk chunk, VillageReference village) {
		String key = village.key();
		if (data.decided(key) || !chunk.newChunk() && !data.pending.contains(key))
			return;
		StructureStart start = start(level, village);
		if (start == null || start.getPieces().isEmpty())
			return;
		BoundingBox center = start.getPieces().get(0).getBoundingBox();
		if (chunk.newChunk() && center.intersects(chunk.pos().getMinBlockX(), chunk.pos().getMinBlockZ(),
			chunk.pos().getMaxBlockX(), chunk.pos().getMaxBlockZ()) && data.pending.add(key))
			data.setDirty();
		if (!data.pending.contains(key) || !allLoaded(level, center))
			return;
		decide(level, data, village, center);
	}

	private static @Nullable StructureStart start(ServerLevel level, VillageReference village) {
		StructureStart[] found = new StructureStart[1];
		level.structureManager().fillStartsForStructure(village.structure(), LongSet.of(village.startChunk()), start -> found[0] = start);
		return found[0];
	}

	/** Places the sign, or records that it could not be placed. Either way the village is decided. */
	private static void decide(ServerLevel level, Data data, VillageReference village, BoundingBox center) {
		String key = village.key();
		data.pending.remove(key);
		data.setDirty();
		BlockPos bell = nearestBell(level, center);
		BlockPos anchor = bell != null ? bell : surface(level, center.getCenter());
		BlockPos spot = findSpot(level, anchor, center);
		if (spot == null) {
			data.failed.add(key);
			WitchercraftMod.LOGGER.warn("No room for a signpost in village {} near [{}, {}, {}]; it gets none (failed villages: {})", key,
				anchor.getX(), anchor.getY(), anchor.getZ(), data.failed.size());
			return;
		}
		String nameKey = chooseName(level, data, village.structureId());
		if (!FastTravelSigns.placeGenerated(level, spot, FastTravelSigns.SOURCE_VILLAGE, nameKey)) {
			data.failed.add(key);
			WitchercraftMod.LOGGER.warn("Could not register the signpost of village {} at [{}, {}, {}]", key, spot.getX(), spot.getY(), spot.getZ());
			return;
		}
		data.placed.add(key);
		if (!nameKey.isEmpty())
			data.usedNames.add(nameKey);
		WitchercraftMod.LOGGER.info("Placed village signpost for {} at [{}, {}, {}] named {}", key, spot.getX(), spot.getY(), spot.getZ(),
			nameKey.isEmpty() ? "(coordinates)" : nameKey);
	}

	private static boolean allLoaded(ServerLevel level, BoundingBox box) {
		for (int chunkX = box.minX() >> 4; chunkX <= box.maxX() >> 4; chunkX++)
			for (int chunkZ = box.minZ() >> 4; chunkZ <= box.maxZ() >> 4; chunkZ++)
				if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null)
					return false;
		return true;
	}

	/** The town-center bell nearest the piece center; vanilla centers have one or two, one rare variant none. */
	private static @Nullable BlockPos nearestBell(ServerLevel level, BoundingBox center) {
		BlockPos middle = center.getCenter();
		BlockPos best = null;
		for (BlockPos pos : BlockPos.betweenClosed(center.minX(), center.minY(), center.minZ(), center.maxX(), center.maxY(), center.maxZ())) {
			if (!level.getBlockState(pos).is(Blocks.BELL))
				continue;
			if (best == null || pos.distSqr(middle) < best.distSqr(middle)
				|| pos.distSqr(middle) == best.distSqr(middle) && pos.asLong() < best.asLong())
				best = pos.immutable();
		}
		return best;
	}

	private static BlockPos surface(ServerLevel level, BlockPos pos) {
		return new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ());
	}

	/**
	 * The nearest free spot for the sign's lower half. First columns within {@link #NEAR_RADIUS} blocks of
	 * the anchor, near its height; if none, the whole town-center piece. Never replaces solid blocks.
	 * Change this method to change what happens when a village center has no room.
	 */
	private static @Nullable BlockPos findSpot(ServerLevel level, BlockPos anchor, BoundingBox center) {
		BlockPos near = search(level, anchor, NEAR_RADIUS, anchor.getY() - SEARCH_DOWN, anchor.getY() + SEARCH_UP, null);
		if (near != null)
			return near;
		int radius = Math.max(Math.max(anchor.getX() - center.minX(), center.maxX() - anchor.getX()),
			Math.max(anchor.getZ() - center.minZ(), center.maxZ() - anchor.getZ())) + 1;
		return search(level, anchor, radius, center.minY() - 1, center.maxY(), center.inflatedBy(1));
	}

	private static @Nullable BlockPos search(ServerLevel level, BlockPos anchor, int radius, int minY, int maxY, @Nullable BoundingBox within) {
		List<int[]> columns = new ArrayList<>();
		for (int dx = -radius; dx <= radius; dx++)
			for (int dz = -radius; dz <= radius; dz++)
				if ((dx != 0 || dz != 0) && (within == null || within.isInside(anchor.getX() + dx, within.minY(), anchor.getZ() + dz)))
					columns.add(new int[] {dx, dz});
		columns.sort(Comparator.<int[]>comparingInt(column -> column[0] * column[0] + column[1] * column[1])
			.thenComparingInt(column -> column[0]).thenComparingInt(column -> column[1]));
		List<Integer> heights = new ArrayList<>();
		for (int y = minY; y <= maxY; y++)
			heights.add(y);
		heights.sort(Comparator.<Integer>comparingInt(y -> Math.abs(y - anchor.getY())).thenComparingInt(y -> -y));
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int[] column : columns)
			for (int y : heights) {
				pos.set(anchor.getX() + column[0], y, anchor.getZ() + column[1]);
				if (validSpot(level, pos))
					return pos.immutable();
			}
		return null;
	}

	private static boolean validSpot(ServerLevel level, BlockPos pos) {
		if (level.isOutsideBuildHeight(pos.below()) || level.isOutsideBuildHeight(pos.above()))
			return false;
		// The sign and every block it updates must be in loaded chunks, so placement never loads terrain.
		for (int dx = -1; dx <= 1; dx += 2)
			for (int dz = -1; dz <= 1; dz += 2)
				if (level.getChunkSource().getChunkNow((pos.getX() + dx) >> 4, (pos.getZ() + dz) >> 4) == null)
					return false;
		BlockPos below = pos.below();
		BlockState floor = level.getBlockState(below);
		if (!floor.isFaceSturdy(level, below, Direction.UP) || !floor.getFluidState().isEmpty() || floor.is(BlockTags.LEAVES)
			|| floor.is(Blocks.BELL))
			return false;
		return free(level.getBlockState(pos)) && free(level.getBlockState(pos.above()));
	}

	private static boolean free(BlockState state) {
		return state.canBeReplaced() && state.getFluidState().isEmpty();
	}

	/** An unused name of this village's kind, else any name of that kind, else "other", else none. */
	private static String chooseName(ServerLevel level, Data data, Identifier structureId) {
		Map<String, List<String>> lists = nameLists();
		List<String> names = lists.getOrDefault(kind(structureId), List.of());
		if (names.isEmpty())
			names = lists.getOrDefault(OTHER_KIND, List.of());
		if (names.isEmpty())
			return "";
		List<String> unused = names.stream().filter(name -> !data.usedNames.contains(name)).toList();
		List<String> pool = unused.isEmpty() ? names : unused;
		return pool.get(level.getRandom().nextInt(pool.size()));
	}

	/** Vanilla village structures are {@code minecraft:village_<kind>}; anything else uses "other". */
	static String kind(Identifier structureId) {
		String path = structureId.getPath();
		if (Identifier.DEFAULT_NAMESPACE.equals(structureId.getNamespace()) && path.startsWith("village_") && path.length() > 8)
			return path.substring(8);
		return OTHER_KIND;
	}

	/** Name keys grouped by kind, read once from the English language file in the mod jar. */
	private static synchronized Map<String, List<String>> nameLists() {
		if (nameLists != null)
			return nameLists;
		Map<String, List<String>> lists = new TreeMap<>();
		try (InputStreamReader reader = new InputStreamReader(ModList.get().getModFileById(WitchercraftMod.MODID).getFile().getContents()
			.openFile("assets/" + WitchercraftMod.MODID + "/lang/en_us.json"), StandardCharsets.UTF_8)) {
			JsonObject language = JsonParser.parseReader(reader).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : language.entrySet()) {
				String key = entry.getKey();
				if (!key.startsWith(NAME_KEY_PREFIX) || !WorldMapPoiInstance.validNameKey(key))
					continue;
				String rest = key.substring(NAME_KEY_PREFIX.length());
				int dot = rest.indexOf('.');
				if (dot > 0 && dot < rest.length() - 1)
					lists.computeIfAbsent(rest.substring(0, dot), ignored -> new ArrayList<>()).add(key);
			}
		} catch (Exception exception) {
			WitchercraftMod.LOGGER.error("Could not read village signpost names; village signposts will use coordinates", exception);
		}
		lists.values().forEach(names -> names.sort(null));
		lists.replaceAll((kind, names) -> List.copyOf(names));
		nameLists = Map.copyOf(lists);
		WitchercraftMod.LOGGER.info("Village signpost names loaded: {}", lists.entrySet().stream()
			.map(entry -> entry.getKey() + "=" + entry.getValue().size()).toList());
		return nameLists;
	}

	private record LoadedChunk(ChunkPos pos, boolean newChunk, List<VillageReference> villages) {
	}

	private record VillageReference(Structure structure, Identifier structureId, long startChunk) {
		/** Same identity the village POI uses: structure and start chunk. */
		private String key() {
			return structureId + "|" + ChunkPos.getX(startChunk) + "," + ChunkPos.getZ(startChunk);
		}
	}

	/** Per-world village signpost decisions. Village keys are {@code <structure>|<startChunkX>,<startChunkZ>}. */
	public static final class Data extends SavedData {
		private static final int FORMAT_VERSION = 1;
		private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "fast_travel/village_signs");
		private static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("format_version", FORMAT_VERSION).forGetter(ignored -> FORMAT_VERSION),
			Codec.STRING.listOf().optionalFieldOf("pending", List.of()).forGetter(data -> List.copyOf(data.pending)),
			Codec.STRING.listOf().optionalFieldOf("placed", List.of()).forGetter(data -> List.copyOf(data.placed)),
			Codec.STRING.listOf().optionalFieldOf("failed", List.of()).forGetter(data -> List.copyOf(data.failed)),
			Codec.STRING.listOf().optionalFieldOf("used_names", List.of()).forGetter(data -> List.copyOf(data.usedNames))
		).apply(instance, Data::new));
		public static final SavedDataType<Data> TYPE = new SavedDataType<>(DATA_ID, Data::new, CODEC);

		/** Eligible villages still waiting for all town-center chunks to be loaded. */
		private final Set<String> pending = new LinkedHashSet<>();
		private final Set<String> placed = new LinkedHashSet<>();
		private final Set<String> failed = new LinkedHashSet<>();
		private final Set<String> usedNames = new HashSet<>();

		public Data() {
		}

		private Data(int formatVersion, List<String> pending, List<String> placed, List<String> failed, List<String> usedNames) {
			if (formatVersion != FORMAT_VERSION)
				WitchercraftMod.LOGGER.warn("Loading village signpost data version {} with reader version {}", formatVersion, FORMAT_VERSION);
			this.pending.addAll(pending);
			this.placed.addAll(placed);
			this.failed.addAll(failed);
			this.usedNames.addAll(usedNames);
		}

		private boolean decided(String key) {
			return placed.contains(key) || failed.contains(key);
		}
	}
}

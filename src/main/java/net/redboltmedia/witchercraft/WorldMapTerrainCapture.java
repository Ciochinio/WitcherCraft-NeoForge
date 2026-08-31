package net.redboltmedia.witchercraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.util.Util;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.LevelResource;

import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-owned capture queue and per-player exploration masks for world-map
 * terrain. A watched chunk is already being sent to a player, so observing this
 * event neither generates nor force-loads terrain.
 */
@EventBusSubscriber
public final class WorldMapTerrainCapture {
	private static final int PROTOTYPE_CAPTURES_PER_TICK = 1;
	private static final int MAX_PENDING_CAPTURES = 32_768;
	private static final int EXPLORATION_SAVE_INTERVAL_TICKS = 200;
	private static final int DIAGNOSTIC_INTERVAL_TICKS = 1200;
	private static final Map<MinecraftServer, ServerState> SERVERS = new ConcurrentHashMap<>();

	private WorldMapTerrainCapture() {
	}

	/** Validate and asynchronously read a bounded batch requested by a client. */
	public static void requestTiles(ServerPlayer player, int requestId, long[] packedPositions) {
		if (requestId <= 0)
			return;
		if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD) || packedPositions.length == 0 || packedPositions.length > 64) {
			PacketDistributor.sendToPlayer(player, new WorldMapTileRequestCompleteMessage(requestId));
			return;
		}
		ServerState state = SERVERS.computeIfAbsent(level.getServer(), ServerState::new);
		if (!state.allowRequests(player.getUUID(), packedPositions.length)) {
			PacketDistributor.sendToPlayer(player, new WorldMapTileRequestCompleteMessage(requestId));
			return;
		}
		java.util.List<CompletableFuture<Optional<WorldMapTerrainTile>>> reads = new java.util.ArrayList<>();
		Set<Long> unique = new HashSet<>();
		for (long packed : packedPositions) {
			if (!unique.add(packed))
				continue;
			ChunkPos pos = ChunkPos.unpack(packed);
			if (!state.isExplored(level, player.getUUID(), packed))
				continue;
			reads.add(CompletableFuture.<Optional<WorldMapTerrainTile>>supplyAsync(() -> WorldMapTerrainTile.read(tilePath(level.getServer(), level.dimension(), pos), pos.x(), pos.z()), Util.ioPool()).exceptionally(exception -> Optional.empty()));
		}
		CompletableFuture.allOf(reads.toArray(CompletableFuture[]::new)).thenRun(() -> level.getServer().execute(() -> {
			if (player.hasDisconnected())
				return;
			for (CompletableFuture<Optional<WorldMapTerrainTile>> read : reads)
				read.join().ifPresent(tile -> PacketDistributor.sendToPlayer(player, WorldMapTileDataMessage.from(tile)));
			PacketDistributor.sendToPlayer(player, new WorldMapTileRequestCompleteMessage(requestId));
		}));
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event) {
		ServerLevel level = event.getLevel();
		if (!level.dimension().equals(Level.OVERWORLD))
			return;
		ServerState state = SERVERS.computeIfAbsent(level.getServer(), ServerState::new);
		state.markExplored(level, event.getPlayer().getUUID(), event.getPos());
		state.enqueue(level, event.getPos(), event.getPlayer().chunkPosition());
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		ServerState state = SERVERS.get(event.getServer());
		if (state != null)
			state.tick();
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		ServerState state = SERVERS.remove(event.getServer());
		if (state != null)
			state.close();
	}

	private static WorldMapTerrainTile capture(LevelChunk chunk, long gameTime) {
		int count = WorldMapTerrainTile.SAMPLE_COUNT;
		short[] groundHeights = new short[count], foliageHeights = new short[count], waterHeights = new short[count];
		java.util.Arrays.fill(groundHeights, WorldMapTerrainTile.NO_HEIGHT);
		java.util.Arrays.fill(foliageHeights, WorldMapTerrainTile.NO_HEIGHT);
		java.util.Arrays.fill(waterHeights, WorldMapTerrainTile.NO_HEIGHT);
		byte[] groundColors = new byte[count], groundTintKinds = new byte[count], foliageColors = new byte[count], foliageTintKinds = new byte[count];
		byte[] decorationKinds = new byte[count], decorationColors = new byte[count], decorationTintKinds = new byte[count];
		int[] groundTints = new int[count], foliageTints = new int[count], waterTints = new int[count], decorationTints = new int[count];
		short[] groundStateIndices = new short[count], foliageStateIndices = new short[count], decorationStateIndices = new short[count];
		StatePalette statePalette = new StatePalette();
		ChunkPos chunkPos = chunk.getPos();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int localZ = 0; localZ < 16; localZ++) {
			for (int localX = 0; localX < 16; localX++) {
				int index = localX + localZ * 16;
				int worldX = chunkPos.getMinBlockX() + localX;
				int worldZ = chunkPos.getMinBlockZ() + localZ;
				int topY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ);
				for (int y = topY; y >= chunk.getMinY(); y--) {
					pos.set(worldX, y, worldZ);
					BlockState state = chunk.getBlockState(pos);
					if (state.isAir())
						continue;
					Biome biome = chunk.getLevel().getBiome(pos).value();
					if (state.getFluidState().is(FluidTags.WATER)) {
						if (waterHeights[index] == WorldMapTerrainTile.NO_HEIGHT) {
							waterHeights[index] = (short)y;
							waterTints[index] = biome.getWaterColor();
						}
						continue;
					}
					if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) {
						if (foliageHeights[index] == WorldMapTerrainTile.NO_HEIGHT) {
							foliageHeights[index] = (short)y;
							foliageStateIndices[index] = statePalette.index(state);
							storeColor(state.getMapColor(chunk, pos), biome, worldX, worldZ, index, foliageColors, foliageTintKinds, foliageTints);
						}
						continue;
					}
					if (state.getCollisionShape(chunk, pos).isEmpty()) {
						MapColor decorationColor = state.getMapColor(chunk, pos);
						if (decorationKinds[index] == 0 && decorationColor != MapColor.NONE) {
							decorationKinds[index] = 1;
							decorationStateIndices[index] = statePalette.index(state);
							storeColor(decorationColor, biome, worldX, worldZ, index, decorationColors, decorationTintKinds, decorationTints);
						}
						continue;
					}
					groundHeights[index] = (short)y;
					groundStateIndices[index] = statePalette.index(state);
					storeColor(state.getMapColor(chunk, pos), biome, worldX, worldZ, index, groundColors, groundTintKinds, groundTints);
					break;
				}
			}
		}
		return new WorldMapTerrainTile(chunkPos.x(), chunkPos.z(), gameTime, groundHeights, groundColors, groundTintKinds, groundTints,
			foliageHeights, foliageColors, foliageTintKinds, foliageTints, waterHeights, waterTints,
			decorationKinds, decorationColors, decorationTintKinds, decorationTints,
			statePalette.entries(), groundStateIndices, foliageStateIndices, decorationStateIndices);
	}

	private static final class StatePalette {
		private final java.util.List<String> entries = new java.util.ArrayList<>(java.util.List.of(""));
		private final Map<String, Short> indices = new HashMap<>();
		private short index(BlockState state) {
			String serialized = BlockStateParser.serialize(state);
			if (serialized.length() > WorldMapTerrainTile.MAX_STATE_LENGTH) return 0;
			Short existing = indices.get(serialized);
			if (existing != null) return existing;
			if (entries.size() >= WorldMapTerrainTile.MAX_PALETTE_SIZE) return 0;
			short index = (short)entries.size(); entries.add(serialized); indices.put(serialized, index); return index;
		}
		private String[] entries() { return entries.toArray(String[]::new); }
	}

	private static void storeColor(MapColor color, Biome biome, int worldX, int worldZ, int index, byte[] colors, byte[] tintKinds, int[] tints) {
		colors[index] = (byte)color.id;
		if (color == MapColor.GRASS) { tintKinds[index] = 1; tints[index] = biome.getGrassColor(worldX, worldZ); }
		else if (color == MapColor.PLANT) { tintKinds[index] = 2; tints[index] = biome.getFoliageColor(); }
		else if (color == MapColor.WATER) { tintKinds[index] = 3; tints[index] = biome.getWaterColor(); }
	}

	private static Path dimensionRoot(MinecraftServer server, ResourceKey<Level> dimension) {
		Path worldRoot = server.getWorldPath(LevelResource.ROOT);
		return DimensionType.getStorageFolder(dimension, worldRoot).resolve("data").resolve("witchercraft_world_map");
	}

	private static Path tilePath(MinecraftServer server, ResourceKey<Level> dimension, ChunkPos pos) {
		return dimensionRoot(server, dimension).resolve("terrain").resolve("r." + (pos.x() >> 5) + "." + (pos.z() >> 5)).resolve("c." + pos.x() + "." + pos.z() + ".wct");
	}

	private static Path explorationPath(MinecraftServer server, ResourceKey<Level> dimension, UUID playerId) {
		return dimensionRoot(server, dimension).resolve("exploration").resolve(playerId + ".wce");
	}

	private record PendingChunk(ResourceKey<Level> dimension, long chunkPos, long distanceSquared, long sequence) {
	}

	private record PlayerKey(ResourceKey<Level> dimension, UUID playerId) {
	}

	private static final class ServerState {
		private final MinecraftServer server;
		private final PriorityQueue<PendingChunk> queue = new PriorityQueue<>(Comparator.comparingLong(PendingChunk::distanceSquared).thenComparingLong(PendingChunk::sequence));
		private final Set<String> queued = new HashSet<>();
		private final Map<PlayerKey, ExplorationMask> exploration = new HashMap<>();
		private final Map<UUID, RequestAllowance> requestAllowances = new HashMap<>();
		private final Set<CompletableFuture<?>> writes = ConcurrentHashMap.newKeySet();
		private final AtomicLong captured = new AtomicLong();
		private final AtomicLong failed = new AtomicLong();
		private long enqueued;
		private long skippedUnloaded;
		private long droppedQueueFull;
		private long sequence;

		private ServerState(MinecraftServer server) {
			this.server = server;
		}

		private void enqueue(ServerLevel level, ChunkPos pos, ChunkPos playerPos) {
			String key = level.dimension().identifier() + ":" + pos.pack();
			if (!queued.add(key))
				return;
			if (queue.size() >= MAX_PENDING_CAPTURES) {
				queued.remove(key);
				droppedQueueFull++;
				return;
			}
			long dx = (long) pos.x() - playerPos.x();
			long dz = (long) pos.z() - playerPos.z();
			queue.add(new PendingChunk(level.dimension(), pos.pack(), dx * dx + dz * dz, sequence++));
			enqueued++;
		}

		private void markExplored(ServerLevel level, UUID playerId, ChunkPos pos) {
			PlayerKey key = new PlayerKey(level.dimension(), playerId);
			ExplorationMask mask = exploration.computeIfAbsent(key, ignored -> ExplorationMask.read(explorationPath(server, level.dimension(), playerId), playerId));
			mask.add(pos.pack());
		}

		private boolean isExplored(ServerLevel level, UUID playerId, long packedPos) {
			PlayerKey key = new PlayerKey(level.dimension(), playerId);
			ExplorationMask mask = exploration.computeIfAbsent(key, ignored -> ExplorationMask.read(explorationPath(server, level.dimension(), playerId), playerId));
			return mask.contains(packedPos);
		}

		private boolean allowRequests(UUID playerId, int count) {
			int tick = server.getTickCount();
			RequestAllowance allowance = requestAllowances.computeIfAbsent(playerId, ignored -> new RequestAllowance(tick));
			return allowance.consume(tick, count);
		}

		private void tick() {
			for (int i = 0; i < PROTOTYPE_CAPTURES_PER_TICK && !queue.isEmpty(); i++) {
				PendingChunk pending = queue.poll();
				ChunkPos pos = ChunkPos.unpack(pending.chunkPos());
				queued.remove(pending.dimension().identifier() + ":" + pending.chunkPos());
				ServerLevel level = server.getLevel(pending.dimension());
				LevelChunk chunk = level == null ? null : level.getChunkSource().getChunkNow(pos.x(), pos.z());
				if (chunk == null) {
					skippedUnloaded++;
					continue;
				}
				WorldMapTerrainTile tile = capture(chunk, level.getGameTime());
				submitWrite(() -> tile.writeAtomically(tilePath(server, pending.dimension(), pos)), true,
					() -> server.execute(() -> PacketDistributor.sendToPlayersTrackingChunk(level, pos, WorldMapTileDataMessage.from(tile))), () -> {
					});
			}
			int tick = server.getTickCount();
			if (tick % EXPLORATION_SAVE_INTERVAL_TICKS == 0)
				flushExploration();
			if (tick % DIAGNOSTIC_INTERVAL_TICKS == 0)
				WitchercraftMod.LOGGER.info("World map capture: queued={}, captured={}, pending={}, skipped_unloaded={}, dropped_queue_full={}, failed={}", enqueued, captured.get(), queue.size(), skippedUnloaded, droppedQueueFull, failed.get());
		}

		private void flushExploration() {
			exploration.forEach((key, mask) -> {
				ExplorationSnapshot snapshot = mask.takeDirtySnapshot();
				if (snapshot != null)
					submitWrite(() -> ExplorationMask.write(explorationPath(server, key.dimension(), key.playerId()), key.playerId(), snapshot.chunks()), false, () -> mask.finishSave(snapshot.revision(), true), () -> mask.finishSave(snapshot.revision(), false));
			});
		}

		private void submitWrite(IoAction action, boolean terrainTile) {
			submitWrite(action, terrainTile, () -> {
			}, () -> {
			});
		}

		private void submitWrite(IoAction action, boolean terrainTile, Runnable onSuccess, Runnable onFailure) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					action.run();
					if (terrainTile)
						captured.incrementAndGet();
					onSuccess.run();
				} catch (Exception exception) {
					failed.incrementAndGet();
					onFailure.run();
					WitchercraftMod.LOGGER.error("World map storage write failed", exception);
				}
			}, Util.ioPool());
			writes.add(future);
			future.whenComplete((ignored, throwable) -> writes.remove(future));
		}

		private void close() {
			flushExploration();
			waitForWrites();
			flushExploration();
			waitForWrites();
		}

		private void waitForWrites() {
			CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new)).join();
		}
	}

	@FunctionalInterface
	private interface IoAction {
		void run() throws IOException;
	}

	private record ExplorationSnapshot(long revision, long[] chunks) {
	}

	private static final class RequestAllowance {
		private static final int WINDOW_TICKS = 20;
		private static final int MAX_TILES_PER_WINDOW = 128;
		private int windowStart;
		private int used;

		private RequestAllowance(int tick) {
			windowStart = tick;
		}

		private boolean consume(int tick, int count) {
			if (tick - windowStart >= WINDOW_TICKS || tick < windowStart) {
				windowStart = tick;
				used = 0;
			}
			if (count > MAX_TILES_PER_WINDOW - used)
				return false;
			used += count;
			return true;
		}
	}

	private static final class ExplorationMask {
		private static final int MAGIC = 0x57434558; // WCEX
		private static final int VERSION = 1;
		private static final int MAX_CHUNKS = 2_000_000;
		private final LongOpenHashSet chunks;
		private boolean dirty;
		private boolean saving;
		private boolean fullWarningLogged;
		private long revision;

		private ExplorationMask(LongOpenHashSet chunks) {
			this.chunks = chunks;
		}

		private synchronized void add(long chunkPos) {
			if (chunks.size() >= MAX_CHUNKS && !chunks.contains(chunkPos)) {
				if (!fullWarningLogged) {
					fullWarningLogged = true;
					WitchercraftMod.LOGGER.error("World map exploration mask reached its hard safety ceiling of {} chunks", MAX_CHUNKS);
				}
				return;
			}
			if (chunks.add(chunkPos)) {
				dirty = true;
				revision++;
			}
		}

		private synchronized boolean contains(long chunkPos) {
			return chunks.contains(chunkPos);
		}

		private synchronized ExplorationSnapshot takeDirtySnapshot() {
			if (!dirty || saving)
				return null;
			saving = true;
			long[] snapshot = chunks.toLongArray();
			java.util.Arrays.sort(snapshot);
			return new ExplorationSnapshot(revision, snapshot);
		}

		private synchronized void finishSave(long savedRevision, boolean success) {
			saving = false;
			if (success && revision == savedRevision)
				dirty = false;
		}

		private static ExplorationMask read(Path source, UUID expectedPlayerId) {
			if (!Files.exists(source))
				return new ExplorationMask(new LongOpenHashSet());
			try {
				byte[] file = Files.readAllBytes(source);
				if (file.length < Integer.BYTES)
					return new ExplorationMask(new LongOpenHashSet());
				int bodyLength = file.length - Integer.BYTES;
				CRC32 crc = new CRC32();
				crc.update(file, 0, bodyLength);
				try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(file))) {
					if (input.readInt() != MAGIC || input.readInt() != VERSION)
						return new ExplorationMask(new LongOpenHashSet());
					UUID playerId = new UUID(input.readLong(), input.readLong());
					int count = input.readInt();
					if (!playerId.equals(expectedPlayerId) || count < 0 || count > MAX_CHUNKS || file.length != 32 + count * Long.BYTES)
						return new ExplorationMask(new LongOpenHashSet());
					LongOpenHashSet chunks = new LongOpenHashSet(Math.max(16, count));
					for (int i = 0; i < count; i++)
						chunks.add(input.readLong());
					if (input.readInt() != (int) crc.getValue())
						return new ExplorationMask(new LongOpenHashSet());
					return new ExplorationMask(chunks);
				}
			} catch (IOException | RuntimeException exception) {
				WitchercraftMod.LOGGER.warn("Ignoring unreadable world map exploration file {}", source, exception);
				return new ExplorationMask(new LongOpenHashSet());
			}
		}

		private static void write(Path target, UUID playerId, long[] chunks) throws IOException {
			ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream(28 + chunks.length * Long.BYTES);
			try (DataOutputStream output = new DataOutputStream(bodyBytes)) {
				output.writeInt(MAGIC);
				output.writeInt(VERSION);
				output.writeLong(playerId.getMostSignificantBits());
				output.writeLong(playerId.getLeastSignificantBits());
				output.writeInt(chunks.length);
				for (long chunk : chunks)
					output.writeLong(chunk);
			}
			byte[] body = bodyBytes.toByteArray();
			CRC32 crc = new CRC32();
			crc.update(body);
			Files.createDirectories(target.getParent());
			Path temporary = target.resolveSibling(target.getFileName() + ".tmp-" + UUID.randomUUID());
			try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(temporary))) {
				output.write(body);
				output.writeInt((int) crc.getValue());
			}
			try {
				Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
}

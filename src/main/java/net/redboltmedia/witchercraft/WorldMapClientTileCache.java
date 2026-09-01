package net.redboltmedia.witchercraft;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

/** Bounded decoded-tile and independently replaceable leaf-texture caches for authorized world-map terrain. */
@EventBusSubscriber(Dist.CLIENT)
public final class WorldMapClientTileCache {
	/*
	 * A drawable leaf covers 4 by 4 chunks. Keeping this unit small is important:
	 * one arriving chunk must not suppress or rebuild a 256 by 256-block image.
	 */
	private static final int CHUNK = 16, REGION_CHUNKS = 4, REGION = 64, LOD_COUNT = 1, PREFETCH_REGIONS = 4;
	private static final int OVERVIEW_CHUNKS = 16, OVERVIEW = 256, OVERVIEW_PIXELS = 256, OVERVIEW_PREFETCH = 1;
	private static final double OVERVIEW_ENTER_ZOOM = 0.65, OVERVIEW_EXIT_ZOOM = 0.85;
	private static final int MAX_TILES = 4096, MAX_REGIONS = 512, MAX_PENDING_BUILDS = 8, MAX_UPLOADS_PER_FRAME = 4, MAX_CACHED_UPLOADS_PER_FRAME = 8;
	private static final long REQUEST_INTERVAL_NANOS = 500_000_000L, DIAGNOSTIC_NANOS = 5_000_000_000L, BACKGROUND_DEBOUNCE_NANOS = 2_000_000_000L;
	private static final LinkedHashMap<Long, DecodedTile> TILES = new LinkedHashMap<>(256, .75f, true);
	private static final LinkedHashMap<Long, ClientRegion> REGIONS = new LinkedHashMap<>(32, .75f, true);
	private static final LinkedHashMap<Long, OverviewPage> OVERVIEWS = new LinkedHashMap<>(32, .75f, true);
	private static final Set<Long> KNOWN_ABSENT = new HashSet<>();
	private static final Set<Long> VALIDATED = new HashSet<>();
	private static final Map<String, TextureSample> BLOCK_TEXTURE_COLORS = new HashMap<>();
	private static final Set<String> FAILED_BLOCK_TEXTURE_COLORS = new HashSet<>();
	private static TextureSample waterTextureColor;
	private static boolean waterTextureColorFailed;
	private static final ArrayDeque<RetiredTexture> RETIRED_TEXTURES = new ArrayDeque<>();
	private static final ConcurrentLinkedQueue<CompletedBuild> COMPLETED_BUILDS = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<CompletedOverviewBuild> COMPLETED_OVERVIEWS = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<CachedRegion> CACHED_REGIONS = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<CachedOverview> CACHED_OVERVIEWS = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<CachedTile> CACHED_TILES = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<Long> CACHE_MISSES = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<Long> STALE_CACHED_REGIONS = new ConcurrentLinkedQueue<>();
	private static final Map<Long, Long> BACKGROUND_DIRTY = new HashMap<>();
	private static final ExecutorService REGION_BUILDER = Executors.newSingleThreadExecutor(task -> {
		Thread thread = new Thread(task, "WitcherCraft world map leaf builder");
		thread.setDaemon(true);
		return thread;
	});
	private static final ScheduledExecutorService CACHE_MAINTENANCE = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "WitcherCraft world map cache maintenance"); thread.setDaemon(true); return thread;
	});
	private static Object connectionIdentity;
	private static int nextRequestId = 1, inFlightRequestId, selectedLod, pendingBuilds;
	private static long[] inFlightPositions = new long[0];
	private static long viewGeneration, inFlightGeneration, cacheGeneration, renderFrame, textureSequence;
	private static long nextRequestNanos;
	private static long lastMapRenderNanos;
	private static boolean viewDirty = true, haveViewBounds, overviewSelected;
	private static int lastMinChunkX, lastMaxChunkX, lastMinChunkZ, lastMaxChunkZ, lastViewportW, lastViewportH;
	private static double lastCenterX, lastCenterZ, lastZoom;
	private static long rebuilds, uploads, draws, frames, renderNanos, lastDiagnostic, requestedTiles, completedBuilds, staleBuilds;
	private static long visualSettingsKey;

	static {
		CACHE_MAINTENANCE.scheduleWithFixedDelay(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft != null) minecraft.execute(() -> maintainCache(minecraft));
		}, 500, 500, TimeUnit.MILLISECONDS);
	}
	private WorldMapClientTileCache() {}
	@SubscribeEvent
	public static void registerReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_block_colors"), (ResourceManagerReloadListener) manager -> {
			BLOCK_TEXTURE_COLORS.clear();
			FAILED_BLOCK_TEXTURE_COLORS.clear();
			waterTextureColor = null;
			waterTextureColorFailed = false;
			for (ClientRegion region : REGIONS.values()) region.revision++;
			for (OverviewPage page : OVERVIEWS.values()) page.revision++;
		});
	}

	public static void renderAndRequest(GuiGraphicsExtractor g, int vx, int vy, int vw, int vh, double centerX, double centerZ, double zoom) {
		long started = System.nanoTime();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
		lastMapRenderNanos = System.nanoTime();
		renderFrame++;
		releaseRetiredTextures(mc);
		ensureConnection(mc);
		rememberView(vw, vh, centerX, centerZ, zoom);
		refreshVisualSettings();
		if (overviewSelected ? zoom >= OVERVIEW_EXIT_ZOOM : zoom <= OVERVIEW_ENTER_ZOOM) overviewSelected = !overviewSelected;
		DiskCache.updateVisible(lastMinChunkX, lastMaxChunkX, lastMinChunkZ, lastMaxChunkZ, centerX, centerZ, visualSettingsKey, cacheGeneration, overviewSelected);
		applyCachedTiles();
		applyCachedRegions(mc);
		applyCachedOverviews(mc);
		applyCompletedBuilds(mc);
		applyCompletedOverviewBuilds(mc);
		selectedLod = 0;
		int anchorX = Math.floorDiv((int)Math.floor(centerX), OVERVIEW) * OVERVIEW;
		int anchorZ = Math.floorDiv((int)Math.floor(centerZ), OVERVIEW) * OVERVIEW;
		g.pose().pushMatrix();
		g.pose().translate(vx + vw / 2.0f, vy + vh / 2.0f);
		g.pose().scale((float)zoom, (float)zoom);
		g.pose().translate((float)(anchorX - centerX), (float)(anchorZ - centerZ));
		if (overviewSelected) {
			renderOverviews(g, centerX, centerZ, anchorX, anchorZ);
			g.pose().popMatrix();
			trimRegions();
			if (viewDirty && inFlightRequestId == 0 && DiskCache.readyOrUnconfigured() && System.nanoTime() >= nextRequestNanos && !startRequest()) viewDirty = false;
			frames++; renderNanos += System.nanoTime() - started; logDiagnostics();
			return;
		}
		drawOverviewBackground(g, anchorX, anchorZ);
		int minRX = Math.floorDiv(lastMinChunkX, REGION_CHUNKS), maxRX = Math.floorDiv(lastMaxChunkX, REGION_CHUNKS);
		int minRZ = Math.floorDiv(lastMinChunkZ, REGION_CHUNKS), maxRZ = Math.floorDiv(lastMaxChunkZ, REGION_CHUNKS);
		List<ClientRegion> visibleRegions = new ArrayList<>();
		for (int rz = minRZ; rz <= maxRZ; rz++) for (int rx = minRX; rx <= maxRX; rx++) {
			long key = ChunkPos.pack(rx, rz);
			ClientRegion region = REGIONS.get(key);
			if (region == null) { region = createRegion(rx, rz); REGIONS.put(key, region); }
			visibleRegions.add(region);
			region.lastUsedFrame = renderFrame;
			RegionTexture texture = region.current(selectedLod);
			if (texture == null) continue;
			int left = rx * REGION - anchorX, top = rz * REGION - anchorZ;
			int sourceSize = REGION >> selectedLod;
			g.blit(RenderPipelines.GUI_TEXTURED, texture.id, left, top, 0, 0, REGION, REGION, sourceSize, sourceSize, sourceSize, sourceSize);
			draws++;
		}
		g.pose().popMatrix();
		visibleRegions.sort(Comparator.comparingDouble(region -> region.distanceSquared(centerX, centerZ)));
		for (ClientRegion region : visibleRegions) {
			if (pendingBuilds >= MAX_PENDING_BUILDS) break;
			queueBuild(region, selectedLod, false);
		}
		if (pendingBuilds < MAX_PENDING_BUILDS) {
			List<int[]> prefetchedRegions = new ArrayList<>();
			for (int rz = minRZ - PREFETCH_REGIONS; rz <= maxRZ + PREFETCH_REGIONS; rz++) for (int rx = minRX - PREFETCH_REGIONS; rx <= maxRX + PREFETCH_REGIONS; rx++) {
				if (rx >= minRX && rx <= maxRX && rz >= minRZ && rz <= maxRZ) continue;
				prefetchedRegions.add(new int[] { rx, rz });
			}
			prefetchedRegions.sort(Comparator.comparingDouble(position -> {
				double dx = (position[0] + 0.5) * REGION - centerX, dz = (position[1] + 0.5) * REGION - centerZ;
				return dx * dx + dz * dz;
			}));
			for (int[] position : prefetchedRegions) {
				if (pendingBuilds >= MAX_PENDING_BUILDS) break;
				long key = ChunkPos.pack(position[0], position[1]);
				ClientRegion region = REGIONS.get(key);
				if (region == null) { region = createRegion(position[0], position[1]); REGIONS.put(key, region); }
				queueBuild(region, selectedLod, false);
			}
		}
		trimRegions();
		if (viewDirty && inFlightRequestId == 0 && DiskCache.readyOrUnconfigured() && System.nanoTime() >= nextRequestNanos && !startRequest()) viewDirty = false;
		frames++; renderNanos += System.nanoTime() - started; logDiagnostics();
	}

	public static boolean canPause() { return !viewDirty && inFlightRequestId == 0; }
	public static void markViewDirty() { viewDirty = true; viewGeneration++; }

	public static void completeRequest(int requestId, boolean accepted, UUID worldId) {
		ensureConnection(Minecraft.getInstance());
		if (worldId.getMostSignificantBits() != 0L || worldId.getLeastSignificantBits() != 0L) {
			var player = Minecraft.getInstance().player;
			if (player != null) DiskCache.configure(worldId, player.getUUID(), cacheGeneration);
		}
		if (requestId == 0) return;
		if (requestId != inFlightRequestId) return;
		if (accepted) for (long position : inFlightPositions) {
			VALIDATED.add(position);
			if (!TILES.containsKey(position) && !DiskCache.hasTile(position)) KNOWN_ABSENT.add(position);
		}
		inFlightRequestId = 0;
		inFlightPositions = new long[0];
		nextRequestNanos = System.nanoTime() + REQUEST_INTERVAL_NANOS;
		if (inFlightGeneration != viewGeneration) viewDirty = true;
	}

	private static void ensureConnection(Minecraft minecraft) {
		if (connectionIdentity != minecraft.getConnection()) { clear(); connectionIdentity = minecraft.getConnection(); }
	}

	public static void accept(WorldMapTileDataMessage message) {
		if (Minecraft.getInstance().getConnection() == null) return;
		WorldMapTerrainTile tile = new WorldMapTerrainTile(message.chunkX(), message.chunkZ(), message.capturedGameTime(), message.groundHeights(), message.groundColors(),
			message.groundTintKinds(), message.groundTints(), message.foliageHeights(), message.foliageColors(), message.foliageTintKinds(), message.foliageTints(),
			message.waterHeights(), message.waterTints(), message.decorationKinds(), message.decorationColors(), message.decorationTintKinds(), message.decorationTints(),
			message.blockStatePalette(), message.groundStateIndices(), message.foliageStateIndices(), message.decorationStateIndices());
		acceptTile(tile, true, true);
	}

	private static void acceptTile(WorldMapTerrainTile source, boolean persist, boolean invalidateRegion) {
		long key = ChunkPos.pack(source.chunkX(), source.chunkZ());
		KNOWN_ABSENT.remove(key); TILES.remove(key);
		if (persist) VALIDATED.add(key); else viewDirty = true;
		DecodedTile tile = new DecodedTile(source.chunkX(), source.chunkZ(), source.capturedGameTime(), source.groundHeights(), source.groundColors(), source.groundTintKinds(), source.groundTints(),
			source.foliageHeights(), source.foliageColors(), source.foliageTintKinds(), source.foliageTints(), source.waterHeights(), source.waterTints(),
			source.decorationKinds(), source.decorationColors(), source.decorationTintKinds(), source.decorationTints(), source.blockStatePalette(),
			source.groundStateIndices(), source.foliageStateIndices(), source.decorationStateIndices());
		tile.resolveSamples();
		TILES.put(key, tile);
		regionForChunk(source.chunkX(), source.chunkZ()).setPresent(source.chunkX(), source.chunkZ(), true);
		if (invalidateRegion) invalidate(source.chunkX(), source.chunkZ());
		if (invalidateRegion) dirtyOverview(source.chunkX(), source.chunkZ());
		if (persist) DiskCache.saveTile(source);
		if (persist) BACKGROUND_DIRTY.put(ChunkPos.pack(Math.floorDiv(source.chunkX(), REGION_CHUNKS), Math.floorDiv(source.chunkZ(), REGION_CHUNKS)), System.nanoTime() + BACKGROUND_DEBOUNCE_NANOS);
		trimTiles();
	}

	public static void clear() {
		for (ClientRegion region : REGIONS.values()) region.retireTextures();
		for (OverviewPage page : OVERVIEWS.values()) page.retireTexture();
		REGIONS.clear(); OVERVIEWS.clear(); TILES.clear(); KNOWN_ABSENT.clear(); VALIDATED.clear(); BACKGROUND_DIRTY.clear(); COMPLETED_BUILDS.clear(); COMPLETED_OVERVIEWS.clear(); CACHED_REGIONS.clear(); CACHED_OVERVIEWS.clear(); CACHED_TILES.clear(); CACHE_MISSES.clear(); STALE_CACHED_REGIONS.clear(); DiskCache.clear();
		inFlightRequestId = 0; inFlightPositions = new long[0]; viewDirty = true; viewGeneration++; cacheGeneration++; haveViewBounds = false; selectedLod = 0; pendingBuilds = 0; nextRequestNanos = 0;
	}

	private static void queueBuild(ClientRegion region, int lod, boolean diskOnly) {
		if (!region.needsBuild(lod) || region.presentChunks.isEmpty()) return;
		long revision = region.revision;
		region.queuedRevisions[lod] = revision;
		Map<Long, DecodedTile> tiles = new HashMap<>();
		int firstChunkX = region.x * REGION_CHUNKS - 1, firstChunkZ = region.z * REGION_CHUNKS - 1;
		for (int z = firstChunkZ; z <= region.z * REGION_CHUNKS + REGION_CHUNKS - 1; z++)
			for (int x = firstChunkX; x <= region.x * REGION_CHUNKS + REGION_CHUNKS - 1; x++) {
				DecodedTile tile = TILES.get(ChunkPos.pack(x, z));
				if (tile != null) tiles.put(ChunkPos.pack(x, z), tile);
			}
		RegionSnapshot snapshot = new RegionSnapshot(region.x, region.z, revision, cacheGeneration, tiles, RenderSettings.capture(), DiskCache.regionPath(region.x, region.z, visualSettingsKey), coverage(region.x, region.z, tiles));
		pendingBuilds++;
		REGION_BUILDER.execute(() -> {
			NativeImage image = null;
			try {
				image = buildImage(snapshot);
				if (image != null && snapshot.imageTarget != null) DiskCache.writeRegion(snapshot.imageTarget, image, snapshot.coverage);
			} catch (RuntimeException exception) {
				WitchercraftMod.LOGGER.error("Failed to build world map region {},{}", snapshot.x, snapshot.z, exception);
			}
			COMPLETED_BUILDS.add(new CompletedBuild(snapshot.x, snapshot.z, lod, snapshot.revision, snapshot.generation, diskOnly, image));
		});
	}

	private static void renderOverviews(GuiGraphicsExtractor g, double centerX, double centerZ, int anchorX, int anchorZ) {
		int minX = Math.floorDiv(lastMinChunkX, OVERVIEW_CHUNKS), maxX = Math.floorDiv(lastMaxChunkX, OVERVIEW_CHUNKS);
		int minZ = Math.floorDiv(lastMinChunkZ, OVERVIEW_CHUNKS), maxZ = Math.floorDiv(lastMaxChunkZ, OVERVIEW_CHUNKS);
		List<OverviewPage> visible = new ArrayList<>();
		for (int z = minZ; z <= maxZ; z++) for (int x = minX; x <= maxX; x++) {
			OverviewPage page = overviewPage(x, z);
			page.lastUsedFrame = renderFrame;
			visible.add(page);
			if (page.texture == null) { drawLeafFallbackForOverview(g, page.x, page.z, anchorX, anchorZ); continue; }
			int left = x * OVERVIEW - anchorX, top = z * OVERVIEW - anchorZ;
			g.blit(RenderPipelines.GUI_TEXTURED, page.texture.id, left, top, 0, 0, OVERVIEW, OVERVIEW, OVERVIEW_PIXELS, OVERVIEW_PIXELS, OVERVIEW_PIXELS, OVERVIEW_PIXELS);
			draws++;
		}
		visible.sort(Comparator.comparingDouble(page -> page.distanceSquared(centerX, centerZ)));
		for (OverviewPage page : visible) { if (pendingBuilds >= MAX_PENDING_BUILDS) break; queueOverviewBuild(page); }
		for (int ring = 1; ring <= OVERVIEW_PREFETCH && pendingBuilds < MAX_PENDING_BUILDS; ring++) {
			for (int z = minZ - ring; z <= maxZ + ring && pendingBuilds < MAX_PENDING_BUILDS; z++) for (int x = minX - ring; x <= maxX + ring && pendingBuilds < MAX_PENDING_BUILDS; x++) {
				if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) continue;
				queueOverviewBuild(overviewPage(x, z));
			}
		}
		trimOverviews();
	}

	private static void drawOverviewBackground(GuiGraphicsExtractor g, int anchorX, int anchorZ) {
		int minX = Math.floorDiv(lastMinChunkX, OVERVIEW_CHUNKS), maxX = Math.floorDiv(lastMaxChunkX, OVERVIEW_CHUNKS);
		int minZ = Math.floorDiv(lastMinChunkZ, OVERVIEW_CHUNKS), maxZ = Math.floorDiv(lastMaxChunkZ, OVERVIEW_CHUNKS);
		for (int z = minZ; z <= maxZ; z++) for (int x = minX; x <= maxX; x++) {
			OverviewPage page = OVERVIEWS.get(ChunkPos.pack(x, z));
			if (page == null || page.texture == null) continue;
			int left = x * OVERVIEW - anchorX, top = z * OVERVIEW - anchorZ;
			g.blit(RenderPipelines.GUI_TEXTURED, page.texture.id, left, top, 0, 0, OVERVIEW, OVERVIEW, OVERVIEW_PIXELS, OVERVIEW_PIXELS, OVERVIEW_PIXELS, OVERVIEW_PIXELS);
			draws++;
		}
	}

	private static void drawLeafFallbackForOverview(GuiGraphicsExtractor g, int overviewX, int overviewZ, int anchorX, int anchorZ) {
		int firstX = overviewX * OVERVIEW_CHUNKS / REGION_CHUNKS, firstZ = overviewZ * OVERVIEW_CHUNKS / REGION_CHUNKS;
		int leaves = OVERVIEW_CHUNKS / REGION_CHUNKS;
		for (int dz = 0; dz < leaves; dz++) for (int dx = 0; dx < leaves; dx++) {
			int x = firstX + dx, z = firstZ + dz;
			ClientRegion leaf = REGIONS.get(ChunkPos.pack(x, z));
			if (leaf == null || leaf.textures[0] == null) continue;
			int left = x * REGION - anchorX, top = z * REGION - anchorZ;
			g.blit(RenderPipelines.GUI_TEXTURED, leaf.textures[0].id, left, top, 0, 0, REGION, REGION, REGION, REGION, REGION, REGION);
			draws++;
		}
	}

	private static void queueOverviewBuild(OverviewPage page) {
		if (!page.needsBuild() || !hasTileInOverview(page.x, page.z) || !DiskCache.readyForOverview(page.x, page.z)) return;
		long revision = page.revision;
		page.queuedRevision = revision;
		Map<Long, DecodedTile> tiles = new HashMap<>();
		int firstX = page.x * OVERVIEW_CHUNKS - 1, firstZ = page.z * OVERVIEW_CHUNKS - 1;
		for (int z = firstZ; z <= page.z * OVERVIEW_CHUNKS + OVERVIEW_CHUNKS - 1; z++) for (int x = firstX; x <= page.x * OVERVIEW_CHUNKS + OVERVIEW_CHUNKS - 1; x++) {
			DecodedTile tile = TILES.get(ChunkPos.pack(x, z));
			if (tile != null) tiles.put(ChunkPos.pack(x, z), tile);
		}
		OverviewSnapshot snapshot = new OverviewSnapshot(page.x, page.z, revision, cacheGeneration, tiles, RenderSettings.capture(), DiskCache.overviewPath(page.x, page.z, visualSettingsKey), coverage(page.x, page.z, OVERVIEW_CHUNKS, tiles));
		pendingBuilds++;
		REGION_BUILDER.execute(() -> {
			NativeImage image = null;
			try {
				image = buildOverviewImage(snapshot);
				if (image != null && snapshot.imageTarget != null) DiskCache.writeRegion(snapshot.imageTarget, image, snapshot.coverage);
			} catch (RuntimeException exception) { WitchercraftMod.LOGGER.error("Failed to build world map overview {},{}", snapshot.x, snapshot.z, exception); }
			COMPLETED_OVERVIEWS.add(new CompletedOverviewBuild(snapshot.x, snapshot.z, snapshot.revision, snapshot.generation, image));
		});
	}

	private static long[] coverage(int regionX, int regionZ, Map<Long, DecodedTile> tiles) {
		return coverage(regionX, regionZ, REGION_CHUNKS, tiles);
	}

	private static long[] coverage(int regionX, int regionZ, int chunksPerSide, Map<Long, DecodedTile> tiles) {
		BitSet coverage = new BitSet(chunksPerSide * chunksPerSide);
		for (long position : tiles.keySet()) {
			ChunkPos pos = ChunkPos.unpack(position);
			if (Math.floorDiv(pos.x(), chunksPerSide) == regionX && Math.floorDiv(pos.z(), chunksPerSide) == regionZ)
				coverage.set(Math.floorMod(pos.x(), chunksPerSide) + Math.floorMod(pos.z(), chunksPerSide) * chunksPerSide);
		}
		return Arrays.copyOf(coverage.toLongArray(), Math.max(4, (chunksPerSide * chunksPerSide + 63) / 64));
	}

	private static void maintainCache(Minecraft minecraft) {
		if (minecraft.getConnection() == null) return;
		applyCompletedBuilds(minecraft);
		if (System.nanoTime() - lastMapRenderNanos < 1_000_000_000L || pendingBuilds > 0 || !DiskCache.readyOrUnconfigured()) return;
		long now = System.nanoTime();
		Iterator<Map.Entry<Long, Long>> iterator = BACKGROUND_DIRTY.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, Long> entry = iterator.next();
			if (entry.getValue() > now) continue;
			iterator.remove();
			ChunkPos pos = ChunkPos.unpack(entry.getKey());
			ClientRegion region = REGIONS.get(entry.getKey());
			if (region == null) { region = createRegion(pos.x(), pos.z()); REGIONS.put(entry.getKey(), region); }
			queueBuild(region, 0, true);
			return;
		}
	}

	private static void applyCachedTiles() {
		while (CACHE_MISSES.poll() != null) viewDirty = true;
		Long staleRegion;
		while ((staleRegion = STALE_CACHED_REGIONS.poll()) != null) BACKGROUND_DIRTY.put(staleRegion, System.nanoTime());
		for (int applied = 0; applied < 64; applied++) {
			CachedTile cached = CACHED_TILES.poll();
			if (cached == null) return;
			DiskCache.finishedTileLoad(cached.position);
			if (cached.generation != cacheGeneration) continue;
			WorldMapTerrainTile tile = cached.tile;
			if (!TILES.containsKey(ChunkPos.pack(tile.chunkX(), tile.chunkZ()))) acceptTile(tile, false, true);
		}
	}

	private static void applyCachedRegions(Minecraft mc) {
		for (int applied = 0; applied < MAX_CACHED_UPLOADS_PER_FRAME; applied++) {
			CachedRegion cached = CACHED_REGIONS.poll();
			if (cached == null) return;
			CachedRegion loaded = cached;
			DiskCache.finishedRegionLoad(loaded.loadingKey);
			if (loaded.generation != cacheGeneration || loaded.settingsKey != visualSettingsKey) { loaded.image.close(); continue; }
			ClientRegion region = regionForChunk(loaded.x * REGION_CHUNKS, loaded.z * REGION_CHUNKS);
			if (region.textures[0] != null) { loaded.image.close(); continue; }
			Identifier id = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID,
				"world_map/cached_region_" + loaded.x + "_" + loaded.z + "_v_" + textureSequence++);
			mc.getTextureManager().register(id, new DynamicTexture(() -> "WitcherCraft cached map region " + loaded.x + "," + loaded.z, loaded.image));
			region.textures[0] = new RegionTexture(id);
			region.builtRevisions[0] = loaded.stale ? 0 : region.revision;
			uploads++;
		}
	}

	private static void applyCachedOverviews(Minecraft mc) {
		for (int applied = 0; applied < MAX_CACHED_UPLOADS_PER_FRAME; applied++) {
			CachedOverview loaded = CACHED_OVERVIEWS.poll();
			if (loaded == null) return;
			DiskCache.finishedOverviewLoad(loaded.loadingKey);
			if (loaded.generation != cacheGeneration || loaded.settingsKey != visualSettingsKey) { loaded.image.close(); continue; }
			OverviewPage page = overviewPage(loaded.x, loaded.z);
			if (page.texture != null) { loaded.image.close(); continue; }
			Identifier id = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map/cached_overview_" + loaded.x + "_" + loaded.z + "_v_" + textureSequence++);
			mc.getTextureManager().register(id, new DynamicTexture(() -> "WitcherCraft cached map overview " + loaded.x + "," + loaded.z, loaded.image));
			page.texture = new RegionTexture(id);
			page.builtRevision = loaded.stale ? 0 : page.revision;
			uploads++;
		}
	}

	private static void applyCompletedBuilds(Minecraft mc) {
		for (int applied = 0; applied < MAX_UPLOADS_PER_FRAME; applied++) {
			CompletedBuild completed = COMPLETED_BUILDS.poll();
			if (completed == null) return;
			if (completed.generation != cacheGeneration) {
				if (completed.image != null) completed.image.close();
				continue;
			}
			pendingBuilds = Math.max(0, pendingBuilds - 1);
			ClientRegion region = REGIONS.get(ChunkPos.pack(completed.x, completed.z));
			if (region == null) {
				if (completed.image != null) completed.image.close();
				continue;
			}
			boolean superseded = region.revision != completed.revision;
			if (superseded) staleBuilds++;
			if (completed.diskOnly) {
				if (completed.image != null) completed.image.close();
				if (region.queuedRevisions[completed.lod] == completed.revision) region.queuedRevisions[completed.lod] = 0;
				completedBuilds++;
				rebuilds++;
				continue;
			}
			RegionTexture old = region.textures[completed.lod];
			if (completed.image == null) {
				// Keep an older valid texture on screen if a replacement could not be published.
			} else {
				Identifier id = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID,
					"world_map/region_" + region.x + "_" + region.z + "_lod_" + completed.lod + "_v_" + textureSequence++);
				mc.getTextureManager().register(id, new DynamicTexture(() -> "WitcherCraft map region " + region.x + "," + region.z + " LOD " + completed.lod, completed.image));
				region.textures[completed.lod] = new RegionTexture(id);
				if (old != null) retireTexture(old.id);
				uploads++;
			}
			/*
			 * A superseded image is still a useful, authorized snapshot. Publish it
			 * now and let needsBuild() schedule one combined follow-up. Discarding it
			 * is what previously left complete regions black during tile bursts.
			 */
			region.builtRevisions[completed.lod] = completed.revision;
			if (region.queuedRevisions[completed.lod] == completed.revision) region.queuedRevisions[completed.lod] = 0;
			completedBuilds++;
			rebuilds++;
		}
	}

	private static void applyCompletedOverviewBuilds(Minecraft mc) {
		for (int applied = 0; applied < MAX_UPLOADS_PER_FRAME; applied++) {
			CompletedOverviewBuild completed = COMPLETED_OVERVIEWS.poll();
			if (completed == null) return;
			pendingBuilds = Math.max(0, pendingBuilds - 1);
			OverviewPage page = OVERVIEWS.get(ChunkPos.pack(completed.x, completed.z));
			if (completed.generation != cacheGeneration || page == null) { if (completed.image != null) completed.image.close(); continue; }
			if (completed.image != null) {
				Identifier id = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map/overview_" + completed.x + "_" + completed.z + "_v_" + textureSequence++);
				mc.getTextureManager().register(id, new DynamicTexture(() -> "WitcherCraft map overview " + completed.x + "," + completed.z, completed.image));
				RegionTexture old = page.texture;
				page.texture = new RegionTexture(id);
				if (old != null) retireTexture(old.id);
				uploads++;
			}
			page.builtRevision = completed.revision;
			if (page.queuedRevision == completed.revision) page.queuedRevision = 0;
			completedBuilds++;
		}
	}

	private static NativeImage buildImage(RegionSnapshot snapshot) {
		int scale = 1, size = REGION;
		NativeImage image = new NativeImage(size, size, false);
		boolean any = false;
		for (int pz = 0; pz < size; pz++) for (int px = 0; px < size; px++) {
			int wx = snapshot.x * REGION + px * scale, wz = snapshot.z * REGION + pz * scale;
			Sample sample = snapshot.sampleAt(wx, wz);
			if (sample == null) { image.setPixelABGR(px, pz, 0); continue; }
			any = true;
			Sample north = snapshot.sampleAt(wx, wz - 1);
			Sample northwest = snapshot.sampleAt(wx - 1, wz - 1);
			Sample west = snapshot.sampleAt(wx - 1, wz);
			int northHeight = north == null ? sample.height : north.height;
			int northwestHeight = northwest == null ? sample.height : northwest.height;
			boolean canopyRelief = sample.foliage || north != null && north.foliage || northwest != null && northwest.foliage;
			int canopyShadowDifference = 0;
			if (!sample.foliage && !sample.water) {
				if (north != null && north.foliage) canopyShadowDifference = Math.max(canopyShadowDifference, north.height - sample.height);
				if (northwest != null && northwest.foliage) canopyShadowDifference = Math.max(canopyShadowDifference, northwest.height - sample.height);
				if (west != null && west.foliage) canopyShadowDifference = Math.max(canopyShadowDifference, west.height - sample.height);
			}
			image.setPixelABGR(px, pz, argbToAbgr(snapshot.settings.shade(sample, northHeight, northwestHeight, canopyRelief, canopyShadowDifference)));
		}
		if (!any) { image.close(); return null; }
		return image;
	}

	private static NativeImage buildOverviewImage(OverviewSnapshot snapshot) {
		int scale = OVERVIEW / OVERVIEW_PIXELS;
		NativeImage image = new NativeImage(OVERVIEW_PIXELS, OVERVIEW_PIXELS, false);
		boolean any = false;
		for (int pz = 0; pz < OVERVIEW_PIXELS; pz++) for (int px = 0; px < OVERVIEW_PIXELS; px++) {
			long alpha = 0, red = 0, green = 0, blue = 0;
			for (int dz = 0; dz < scale; dz++) for (int dx = 0; dx < scale; dx++) {
				int wx = snapshot.x * OVERVIEW + px * scale + dx, wz = snapshot.z * OVERVIEW + pz * scale + dz;
				Sample sample = snapshot.sampleAt(wx, wz);
				if (sample == null) continue;
				int color = shadeSample(snapshot, sample, wx, wz);
				int a = color >>> 24;
				alpha += a; red += (long)((color >>> 16) & 255) * a; green += (long)((color >>> 8) & 255) * a; blue += (long)(color & 255) * a;
			}
			if (alpha == 0) { image.setPixelABGR(px, pz, 0); continue; }
			any = true;
			int samples = scale * scale;
			int a = (int)Math.min(255, alpha / samples);
			int color = a << 24 | (int)(red / alpha) << 16 | (int)(green / alpha) << 8 | (int)(blue / alpha);
			image.setPixelABGR(px, pz, argbToAbgr(color));
		}
		if (!any) { image.close(); return null; }
		return image;
	}

	private static int shadeSample(OverviewSnapshot snapshot, Sample sample, int wx, int wz) {
		Sample north = snapshot.sampleAt(wx, wz - 1), northwest = snapshot.sampleAt(wx - 1, wz - 1), west = snapshot.sampleAt(wx - 1, wz);
		int northHeight = north == null ? sample.height : north.height;
		int northwestHeight = northwest == null ? sample.height : northwest.height;
		boolean canopyRelief = sample.foliage || north != null && north.foliage || northwest != null && northwest.foliage;
		int canopyShadowDifference = 0;
		if (!sample.foliage && !sample.water) {
			if (north != null && north.foliage) canopyShadowDifference = Math.max(canopyShadowDifference, north.height - sample.height);
			if (northwest != null && northwest.foliage) canopyShadowDifference = Math.max(canopyShadowDifference, northwest.height - sample.height);
			if (west != null && west.foliage) canopyShadowDifference = Math.max(canopyShadowDifference, west.height - sample.height);
		}
		return snapshot.settings.shade(sample, northHeight, northwestHeight, canopyRelief, canopyShadowDifference);
	}

	private static Sample sampleAt(int wx, int wz) {
		DecodedTile tile = tileAt(wx, wz);
		if (tile == null) return null;
		int index = Math.floorMod(wx, CHUNK) + Math.floorMod(wz, CHUNK) * CHUNK;
		return tile.samples[index];
	}

	private static Sample columnSample(DecodedTile tile, int index) {
		int groundHeight = tile.groundHeights[index] == WorldMapTerrainTile.NO_HEIGHT ? 0 : tile.groundHeights[index];
		LayerSample ground = layerSample(tile, tile.groundColors[index], tile.groundTintKinds[index], tile.groundTints[index], tile.groundStateIndices[index]);
		if (tile.waterHeights[index] != WorldMapTerrainTile.NO_HEIGHT) {
			int depth = tile.groundHeights[index] == WorldMapTerrainTile.NO_HEIGHT ? 1 : Math.max(1, tile.waterHeights[index] - tile.groundHeights[index]);
			TextureSample texture = waterTextureColor();
			int tint = 0xFF000000 | tile.waterTints[index];
			int water = texture == null ? scaleColor(tint, 0.88) : mix(multiply(texture.argb, tint), tint, 60, 100);
			double depthFactor = 1.0 - Math.exp(-depth / 8.0);
			double opacity = 0.50 + 0.30 * depthFactor;
			int color = ground.argb == 0 ? water : blend(ground.argb, water, opacity);
			return new Sample(scaleColor(color, 1.0 - 0.18 * depthFactor), tile.waterHeights[index], true, false);
		}
		if (tile.foliageHeights[index] != WorldMapTerrainTile.NO_HEIGHT) {
			LayerSample foliage = layerSample(tile, tile.foliageColors[index], tile.foliageTintKinds[index], tile.foliageTints[index], tile.foliageStateIndices[index]);
			double opacity = Math.min(1.0, foliage.opacity * WorldMapClientConfig.foliageOpacityScale());
			if (foliage.argb != 0 && opacity > 0.0) return new Sample(ground.argb == 0 ? foliage.argb : blend(ground.argb, foliage.argb, opacity), tile.foliageHeights[index], false, true);
		}
		if (WorldMapClientConfig.showDecorations() && tile.decorationKinds[index] != 0) {
			LayerSample decoration = layerSample(tile, tile.decorationColors[index], tile.decorationTintKinds[index], tile.decorationTints[index], tile.decorationStateIndices[index]);
			double opacity = Math.min(1.0, Math.max(0.65, decoration.opacity) * WorldMapClientConfig.decorationOpacityScale());
			if (decoration.argb != 0 && opacity > 0.0) return new Sample(ground.argb == 0 ? decoration.argb : blend(ground.argb, decoration.argb, opacity), groundHeight, false, false);
		}
		return new Sample(ground.argb, groundHeight, false, false);
	}

	private static DecodedTile tileAt(int wx, int wz) {
		return TILES.get(ChunkPos.pack(Math.floorDiv(wx, CHUNK), Math.floorDiv(wz, CHUNK)));
	}

	private static LayerSample layerSample(DecodedTile tile, byte colorId, byte tintKind, int tint, short stateIndex) {
		int base = MapColor.byId(Byte.toUnsignedInt(colorId)).calculateARGBColor(MapColor.Brightness.NORMAL);
		TextureSample texture = null;
		int paletteIndex = Short.toUnsignedInt(stateIndex);
		if (paletteIndex > 0 && paletteIndex < tile.blockStatePalette.length) {
			String state = tile.blockStatePalette[paletteIndex]; TextureSample resolved = BLOCK_TEXTURE_COLORS.get(state);
			if (resolved == null && !FAILED_BLOCK_TEXTURE_COLORS.contains(state)) {
				resolved = resolveBlockTextureSample(state);
				if (resolved == null) FAILED_BLOCK_TEXTURE_COLORS.add(state); else BLOCK_TEXTURE_COLORS.put(state, resolved);
			}
			if (resolved != null) { texture = resolved; base = resolved.argb; }
		}
		double opacity = texture == null ? 0.75 : texture.coverage;
		if (base == 0 || tintKind == 0) return new LayerSample(base, opacity);
		if (texture == null) {
			int biome = scaleColor(0xFF000000 | tint, 0.82);
			return new LayerSample(mix(base, biome, (int)Math.round(WorldMapClientConfig.biomeColorStrength() * 35.0), 100), opacity);
		}
		if (!texture.tinted) return new LayerSample(base, opacity);
		int tinted = multiply(base, 0xFF000000 | tint);
		return new LayerSample(mix(base, tinted, (int)Math.round(WorldMapClientConfig.biomeColorStrength() * 100.0), 100), opacity);
	}
	private static TextureSample resolveBlockTextureSample(String serializedState) {
		try {
			BlockState state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, serializedState, false).blockState();
			BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
			List<BlockStateModelPart> parts = new ArrayList<>(); model.collectParts(RandomSource.create(serializedState.hashCode()), parts);
			BakedQuad selected = null, fallback = null; double selectedArea = -1.0, fallbackArea = -1.0;
			for (BlockStateModelPart part : parts) {
				for (BakedQuad quad : part.getQuads(Direction.UP)) { double area = quadArea(quad); if (area > selectedArea) { selected = quad; selectedArea = area; } }
				for (BakedQuad quad : part.getQuads(null)) {
					double area = quadArea(quad);
					if (area > fallbackArea) { fallback = quad; fallbackArea = area; }
					if (quad.direction() == Direction.UP && area > selectedArea) { selected = quad; selectedArea = area; }
				}
			}
			if (selected == null) selected = fallback;
			TextureAtlasSprite sprite = selected == null ? model.particleMaterial().sprite() : selected.materialInfo().sprite();
			boolean flower = state.is(BlockTags.FLOWERS);
			boolean tinted = selected != null && selected.materialInfo().isTinted() && !flower;
			return averageSprite(sprite, tinted, flower);
		} catch (CommandSyntaxException | RuntimeException exception) {
			return null;
		}
	}
	private static TextureSample waterTextureColor() {
		if (waterTextureColor != null) return waterTextureColor;
		if (waterTextureColorFailed) return null;
		try {
			FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(Fluids.WATER.defaultFluidState());
			waterTextureColor = averageSprite(model.stillMaterial().sprite(), false, false);
		} catch (RuntimeException ignored) {
			waterTextureColorFailed = true;
			return null;
		}
		if (waterTextureColor == null) waterTextureColorFailed = true;
		return waterTextureColor;
	}
	private static double quadArea(BakedQuad quad) {
		double ax=quad.position1().x()-quad.position0().x(), ay=quad.position1().y()-quad.position0().y(), az=quad.position1().z()-quad.position0().z();
		double bx=quad.position2().x()-quad.position0().x(), by=quad.position2().y()-quad.position0().y(), bz=quad.position2().z()-quad.position0().z();
		double cx=ay*bz-az*by, cy=az*bx-ax*bz, cz=ax*by-ay*bx;
		return Math.sqrt(cx*cx+cy*cy+cz*cz) * 0.5;
	}
	private static TextureSample averageSprite(TextureAtlasSprite sprite, boolean tinted, boolean flower) {
		NativeImage image = sprite.contents().getOriginalImage(); long r=0,g=0,b=0,weight=0;
		List<SpritePixel> flowerCandidates = flower ? new ArrayList<>() : List.of();
		for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
			int pixel=image.getPixel(x,y),alpha=pixel>>>24;if(alpha<=10)continue;
			int red=pixel>>16&255,green=pixel>>8&255,blue=pixel&255;
			r+=(long)red*alpha;g+=(long)green*alpha;b+=(long)blue*alpha;weight+=alpha;
			if (flower && !(green * 100 > red * 115 && green * 100 > blue * 115)) {
				int maximum=Math.max(red,Math.max(green,blue)),minimum=Math.min(red,Math.min(green,blue));
				double saturation=maximum==0?0.0:(maximum-minimum)/(double)maximum;
				flowerCandidates.add(new SpritePixel(red,green,blue,alpha,maximum*(0.55+0.45*saturation)));
			}
		}
		if (weight == 0) return null;
		long coverageWeight=weight;
		if (!flowerCandidates.isEmpty()) {
			flowerCandidates.sort(Comparator.comparingDouble(SpritePixel::score).reversed());
			int selectedCount=Math.max(1,(flowerCandidates.size()+3)/4);
			r=g=b=weight=0;
			for(int i=0;i<selectedCount;i++){
				SpritePixel pixel=flowerCandidates.get(i);
				r+=(long)pixel.red*pixel.alpha;g+=(long)pixel.green*pixel.alpha;b+=(long)pixel.blue*pixel.alpha;weight+=pixel.alpha;
			}
		}
		int color = 0xFF000000|(int)(r/weight)<<16|(int)(g/weight)<<8|(int)(b/weight);
		double coverage = Math.min(1.0, coverageWeight / (255.0 * image.getWidth() * image.getHeight()));
		return new TextureSample(color, coverage, tinted);
	}
	private static int multiply(int base, int tint) {
		return 0xFF000000 | (base>>16&255)*(tint>>16&255)/255<<16 | (base>>8&255)*(tint>>8&255)/255<<8 | (base&255)*(tint&255)/255;
	}
	private static int mix(int base, int overlay, int overlayWeight, int totalWeight) {
		int baseWeight = totalWeight - overlayWeight;
		int r = ((base >> 16 & 255) * baseWeight + (overlay >> 16 & 255) * overlayWeight) / totalWeight;
		int g = ((base >> 8 & 255) * baseWeight + (overlay >> 8 & 255) * overlayWeight) / totalWeight;
		int b = ((base & 255) * baseWeight + (overlay & 255) * overlayWeight) / totalWeight;
		return 0xFF000000 | r << 16 | g << 8 | b;
	}
	private static int blend(int base, int overlay, double opacity) {
		int weight = Math.max(0, Math.min(1000, (int)Math.round(opacity * 1000.0)));
		return mix(base, overlay, weight, 1000);
	}
	private static int scaleColor(int color, double factor) {
		return 0xFF000000 | Math.min(255,(int)((color>>16&255)*factor))<<16 | Math.min(255,(int)((color>>8&255)*factor))<<8 | Math.min(255,(int)((color&255)*factor));
	}

	private static int shade(int argb, int height, int northHeight, int northwestHeight, boolean water, boolean canopyRelief, int canopyShadowDifference) {
		if (argb == 0) return 0;
		double sensitivity = WorldMapClientConfig.hillshadeSlopeSensitivity();
		int northBand = slopeBand(height - northHeight, sensitivity);
		int northwestBand = slopeBand(height - northwestHeight, sensitivity);
		double strength = WorldMapClientConfig.hillshadeStrength() * (water ? 0.35 : 1.0);
		if (canopyRelief) strength *= WorldMapClientConfig.canopyReliefStrength();
		double factor = 1.0 + (northBand * 0.12 + northwestBand * 0.06) * strength;
		factor = Math.max(0.65, Math.min(1.35, factor));
		if (canopyShadowDifference > 0) {
			double shadow = WorldMapClientConfig.canopyShadowStrength() * Math.min(1.0, canopyShadowDifference / 12.0);
			factor = Math.max(0.5, factor * (1.0 - shadow));
		}
		factor *= WorldMapClientConfig.terrainBrightness();
		return scaleColor(argb, factor);
	}
	private static int slopeBand(int difference, double sensitivity) {
		double adjusted = Math.abs(difference) * sensitivity;
		if (adjusted < 0.5) return 0;
		int band = adjusted < 1.5 ? 1 : adjusted < 3.5 ? 2 : 3;
		return difference < 0 ? -band : band;
	}
	private static void refreshVisualSettings() {
		long key = WorldMapClientConfig.showDecorations() ? 1 : 0;
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.terrainBrightness());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.biomeColorStrength());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.hillshadeStrength());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.hillshadeSlopeSensitivity());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.canopyReliefStrength());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.canopyShadowStrength());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.foliageOpacityScale());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.decorationOpacityScale());
		for (var pack : Minecraft.getInstance().getResourceManager().listPacks().toList()) key = 31 * key + pack.packId().hashCode();
		if (key == visualSettingsKey) return;
		visualSettingsKey = key;
		for (DecodedTile tile : TILES.values()) tile.resolveSamples();
		for (ClientRegion region : REGIONS.values()) region.revision++;
		for (OverviewPage page : OVERVIEWS.values()) page.revision++;
	}

	private static void invalidate(int chunkX, int chunkZ) {
		int rx = Math.floorDiv(chunkX, REGION_CHUNKS), rz = Math.floorDiv(chunkZ, REGION_CHUNKS);
		boolean eastEdge = Math.floorMod(chunkX, REGION_CHUNKS) == REGION_CHUNKS - 1;
		boolean southEdge = Math.floorMod(chunkZ, REGION_CHUNKS) == REGION_CHUNKS - 1;
		dirty(rx, rz);
		if (eastEdge) dirty(rx + 1, rz);
		if (southEdge) dirty(rx, rz + 1);
		if (eastEdge && southEdge) dirty(rx + 1, rz + 1);
	}
	private static void dirtyOverview(int chunkX, int chunkZ) {
		int ox = Math.floorDiv(chunkX, OVERVIEW_CHUNKS), oz = Math.floorDiv(chunkZ, OVERVIEW_CHUNKS);
		boolean eastEdge = Math.floorMod(chunkX, OVERVIEW_CHUNKS) == OVERVIEW_CHUNKS - 1;
		boolean southEdge = Math.floorMod(chunkZ, OVERVIEW_CHUNKS) == OVERVIEW_CHUNKS - 1;
		dirtyOverviewPage(ox, oz);
		if (eastEdge) dirtyOverviewPage(ox + 1, oz);
		if (southEdge) dirtyOverviewPage(ox, oz + 1);
		if (eastEdge && southEdge) dirtyOverviewPage(ox + 1, oz + 1);
	}
	private static void dirtyOverviewPage(int x, int z) { OverviewPage page = OVERVIEWS.get(ChunkPos.pack(x, z)); if (page != null) page.revision++; }
	private static OverviewPage overviewPage(int x, int z) { return OVERVIEWS.computeIfAbsent(ChunkPos.pack(x, z), ignored -> new OverviewPage(x, z)); }
	private static boolean hasTileInOverview(int x, int z) {
		int firstX = x * OVERVIEW_CHUNKS, firstZ = z * OVERVIEW_CHUNKS;
		for (int dz = 0; dz < OVERVIEW_CHUNKS; dz++) for (int dx = 0; dx < OVERVIEW_CHUNKS; dx++) if (TILES.containsKey(ChunkPos.pack(firstX + dx, firstZ + dz))) return true;
		return false;
	}
	private static ClientRegion regionForChunk(int chunkX, int chunkZ) {
		int rx = Math.floorDiv(chunkX, REGION_CHUNKS), rz = Math.floorDiv(chunkZ, REGION_CHUNKS);
		long key = ChunkPos.pack(rx, rz); ClientRegion region = REGIONS.get(key);
		if (region == null) { region = createRegion(rx, rz); REGIONS.put(key, region); }
		return region;
	}
	private static ClientRegion createRegion(int rx, int rz) {
		ClientRegion region = new ClientRegion(rx, rz);
		for (int localZ = 0; localZ < REGION_CHUNKS; localZ++) for (int localX = 0; localX < REGION_CHUNKS; localX++) {
			int chunkX = rx * REGION_CHUNKS + localX, chunkZ = rz * REGION_CHUNKS + localZ;
			if (TILES.containsKey(ChunkPos.pack(chunkX, chunkZ))) region.setPresent(chunkX, chunkZ, true);
		}
		return region;
	}
	private static void dirty(int rx, int rz) { ClientRegion r = REGIONS.get(ChunkPos.pack(rx, rz)); if (r != null) r.revision++; }

	private static void rememberView(int vw, int vh, double centerX, double centerZ, double zoom) {
		int minX = (int)Math.floor((centerX - vw / (2.0 * zoom)) / CHUNK) - 1;
		int maxX = (int)Math.floor((centerX + vw / (2.0 * zoom)) / CHUNK) + 1;
		int minZ = (int)Math.floor((centerZ - vh / (2.0 * zoom)) / CHUNK) - 1;
		int maxZ = (int)Math.floor((centerZ + vh / (2.0 * zoom)) / CHUNK) + 1;
		if (!haveViewBounds || minX != lastMinChunkX || maxX != lastMaxChunkX || minZ != lastMinChunkZ || maxZ != lastMaxChunkZ) {
			markViewDirty(); lastMinChunkX = minX; lastMaxChunkX = maxX; lastMinChunkZ = minZ; lastMaxChunkZ = maxZ; haveViewBounds = true;
		}
		lastViewportW = vw; lastViewportH = vh; lastCenterX = centerX; lastCenterZ = centerZ; lastZoom = zoom;
	}

	private static boolean startRequest() {
		if (!haveViewBounds || inFlightRequestId != 0) return false;
		PriorityQueue<TileDistance> nearest = new PriorityQueue<>(Comparator.comparingDouble(TileDistance::distanceSquared).reversed());
		for (int z = lastMinChunkZ; z <= lastMaxChunkZ; z++) for (int x = lastMinChunkX; x <= lastMaxChunkX; x++) {
			long key = ChunkPos.pack(x, z); if (VALIDATED.contains(key) || KNOWN_ABSENT.contains(key)) continue;
			if (!TILES.containsKey(key) && DiskCache.hasTile(key)) continue;
			double dx = x * CHUNK + 8 - lastCenterX, dz = z * CHUNK + 8 - lastCenterZ;
			nearest.add(new TileDistance(key, dx * dx + dz * dz));
			if (nearest.size() > WorldMapTileRequestMessage.MAX_POSITIONS) nearest.poll();
		}
		if (nearest.isEmpty()) return false;
		List<TileDistance> ordered = new ArrayList<>(nearest); ordered.sort(Comparator.comparingDouble(TileDistance::distanceSquared));
		long[] positions = new long[ordered.size()];
		long[] capturedTimes = new long[ordered.size()];
		for (int i = 0; i < ordered.size(); i++) {
			positions[i] = ordered.get(i).position;
			DecodedTile tile = TILES.get(positions[i]); capturedTimes[i] = tile == null ? -1L : tile.capturedGameTime;
		}
		int id = nextRequestId++; if (nextRequestId <= 0) nextRequestId = 1;
		inFlightRequestId = id; inFlightGeneration = viewGeneration; inFlightPositions = positions; requestedTiles += positions.length;
		ClientPacketDistributor.sendToServer(new WorldMapTileRequestMessage(id, positions, capturedTimes)); return true;
	}

	private static void trimTiles() {
		Iterator<DecodedTile> it = TILES.values().iterator();
		while (TILES.size() > MAX_TILES && it.hasNext()) {
			DecodedTile tile = it.next();
			int prefetchChunks = PREFETCH_REGIONS * REGION_CHUNKS;
			if (haveViewBounds && tile.chunkX >= lastMinChunkX - prefetchChunks && tile.chunkX <= lastMaxChunkX + prefetchChunks
				&& tile.chunkZ >= lastMinChunkZ - prefetchChunks && tile.chunkZ <= lastMaxChunkZ + prefetchChunks) continue;
			it.remove();
			regionForChunk(tile.chunkX, tile.chunkZ).setPresent(tile.chunkX, tile.chunkZ, false); invalidate(tile.chunkX, tile.chunkZ);
		}
	}
	private static void trimRegions() {
		Iterator<ClientRegion> it = REGIONS.values().iterator();
		while (REGIONS.size() > MAX_REGIONS && it.hasNext()) {
			ClientRegion region = it.next();
			if (region.lastUsedFrame == renderFrame) continue;
			it.remove();
			region.retireTextures();
		}
	}
	private static void trimOverviews() {
		Iterator<OverviewPage> it = OVERVIEWS.values().iterator();
		while (OVERVIEWS.size() > 64 && it.hasNext()) {
			OverviewPage page = it.next();
			if (page.lastUsedFrame == renderFrame) continue;
			it.remove(); page.retireTexture();
		}
	}
	private static void retireTexture(Identifier id) {
		RETIRED_TEXTURES.addLast(new RetiredTexture(id, renderFrame + 1));
	}
	private static void releaseRetiredTextures(Minecraft mc) {
		while (!RETIRED_TEXTURES.isEmpty() && RETIRED_TEXTURES.peekFirst().releaseFrame <= renderFrame)
			mc.getTextureManager().release(RETIRED_TEXTURES.removeFirst().id);
	}

	private static void logDiagnostics() {
		long now = System.nanoTime(); if (lastDiagnostic == 0) { lastDiagnostic = now; return; }
		if (now - lastDiagnostic < DIAGNOSTIC_NANOS) return;
		long cpu = TILES.size() * 256L * 29, gpu = 0; int overviewTextures = 0; for (ClientRegion r : REGIONS.values()) gpu += r.gpuBytes(); for (OverviewPage page : OVERVIEWS.values()) if (page.texture != null) { gpu += OVERVIEW_PIXELS * OVERVIEW_PIXELS * 4L; overviewTextures++; }
		double ms = frames == 0 ? 0 : renderNanos / 1_000_000.0 / frames;
		WitchercraftMod.LOGGER.debug("World map renderer: decoded_tiles={}, cpu_kib={}, gpu_leaves={}, gpu_overviews={}, gpu_kib={}, overview_selected={}, requested_tiles={}, pending_builds={}, completed_builds={}, superseded_builds={}, rebuilds={}, uploads={}, draw_calls={}, avg_render_ms={}",
			TILES.size(), cpu / 1024, REGIONS.size(), overviewTextures, gpu / 1024, overviewSelected, requestedTiles, pendingBuilds, completedBuilds, staleBuilds, rebuilds, uploads, draws, String.format(Locale.ROOT, "%.3f", ms));
		requestedTiles = completedBuilds = staleBuilds = rebuilds = uploads = draws = frames = renderNanos = 0; lastDiagnostic = now;
	}
	private static int argbToAbgr(int c) { return c & 0xFF00FF00 | (c & 0x00FF0000) >> 16 | (c & 0xFF) << 16; }

	private record TileDistance(long position, double distanceSquared) {}
	private record SpritePixel(int red, int green, int blue, int alpha, double score) {}
	private record TextureSample(int argb, double coverage, boolean tinted) {}
	private record LayerSample(int argb, double opacity) {}
	private record Sample(int argb, int height, boolean water, boolean foliage) {}
	private record RetiredTexture(Identifier id, long releaseFrame) {}
	private record CompletedBuild(int x, int z, int lod, long revision, long generation, boolean diskOnly, NativeImage image) {}
	private record CompletedOverviewBuild(int x, int z, long revision, long generation, NativeImage image) {}
	private record RenderSettings(double brightness, double hillshadeStrength, double slopeSensitivity, double canopyReliefStrength, double canopyShadowStrength) {
		static RenderSettings capture() {
			return new RenderSettings(WorldMapClientConfig.terrainBrightness(), WorldMapClientConfig.hillshadeStrength(), WorldMapClientConfig.hillshadeSlopeSensitivity(),
				WorldMapClientConfig.canopyReliefStrength(), WorldMapClientConfig.canopyShadowStrength());
		}
		int shade(Sample sample, int northHeight, int northwestHeight, boolean canopyRelief, int canopyShadowDifference) {
			if (sample.argb == 0) return 0;
			int northBand = slopeBand(sample.height - northHeight, slopeSensitivity);
			int northwestBand = slopeBand(sample.height - northwestHeight, slopeSensitivity);
			double strength = hillshadeStrength * (sample.water ? 0.35 : 1.0);
			if (canopyRelief) strength *= canopyReliefStrength;
			double factor = 1.0 + (northBand * 0.12 + northwestBand * 0.06) * strength;
			factor = Math.max(0.65, Math.min(1.35, factor));
			if (canopyShadowDifference > 0) {
				double shadow = canopyShadowStrength * Math.min(1.0, canopyShadowDifference / 12.0);
				factor = Math.max(0.5, factor * (1.0 - shadow));
			}
			return scaleColor(sample.argb, factor * brightness);
		}
	}
	private record RegionSnapshot(int x, int z, long revision, long generation, Map<Long, DecodedTile> tiles, RenderSettings settings, Path imageTarget, long[] coverage) {
		Sample sampleAt(int wx, int wz) {
			DecodedTile tile = tiles.get(ChunkPos.pack(Math.floorDiv(wx, CHUNK), Math.floorDiv(wz, CHUNK)));
			if (tile == null) return null;
			return tile.samples[Math.floorMod(wx, CHUNK) + Math.floorMod(wz, CHUNK) * CHUNK];
		}
	}
	private record OverviewSnapshot(int x, int z, long revision, long generation, Map<Long, DecodedTile> tiles, RenderSettings settings, Path imageTarget, long[] coverage) {
		Sample sampleAt(int wx, int wz) {
			DecodedTile tile = tiles.get(ChunkPos.pack(Math.floorDiv(wx, CHUNK), Math.floorDiv(wz, CHUNK)));
			if (tile == null) return null;
			return tile.samples[Math.floorMod(wx, CHUNK) + Math.floorMod(wz, CHUNK) * CHUNK];
		}
	}
	private record CachedRegion(int x, int z, long settingsKey, long generation, boolean stale, String loadingKey, NativeImage image) {}
	private record CachedOverview(int x, int z, long settingsKey, long generation, boolean stale, String loadingKey, NativeImage image) {}
	private record CachedTile(long position, WorldMapTerrainTile tile, long generation) {}
	private static final class DecodedTile {
		final int chunkX, chunkZ; final long capturedGameTime; final short[] groundHeights; final byte[] groundColors, groundTintKinds; final int[] groundTints;
		final short[] foliageHeights; final byte[] foliageColors, foliageTintKinds; final int[] foliageTints;
		final short[] waterHeights; final int[] waterTints; final byte[] decorationKinds, decorationColors, decorationTintKinds; final int[] decorationTints;
		final String[] blockStatePalette; final short[] groundStateIndices, foliageStateIndices, decorationStateIndices; volatile Sample[] samples;
		DecodedTile(int chunkX, int chunkZ, long capturedGameTime, short[] groundHeights, byte[] groundColors, byte[] groundTintKinds, int[] groundTints,
			short[] foliageHeights, byte[] foliageColors, byte[] foliageTintKinds, int[] foliageTints, short[] waterHeights, int[] waterTints,
			byte[] decorationKinds, byte[] decorationColors, byte[] decorationTintKinds, int[] decorationTints, String[] blockStatePalette,
			short[] groundStateIndices, short[] foliageStateIndices, short[] decorationStateIndices) {
			this.chunkX=chunkX; this.chunkZ=chunkZ; this.capturedGameTime=capturedGameTime; this.groundHeights=groundHeights; this.groundColors=groundColors; this.groundTintKinds=groundTintKinds; this.groundTints=groundTints;
			this.foliageHeights=foliageHeights; this.foliageColors=foliageColors; this.foliageTintKinds=foliageTintKinds; this.foliageTints=foliageTints;
			this.waterHeights=waterHeights; this.waterTints=waterTints; this.decorationKinds=decorationKinds; this.decorationColors=decorationColors;
			this.decorationTintKinds=decorationTintKinds; this.decorationTints=decorationTints; this.blockStatePalette=blockStatePalette;
			this.groundStateIndices=groundStateIndices; this.foliageStateIndices=foliageStateIndices; this.decorationStateIndices=decorationStateIndices;
		}
		void resolveSamples() {
			Sample[] resolved = new Sample[WorldMapTerrainTile.SAMPLE_COUNT];
			for (int i = 0; i < resolved.length; i++) resolved[i] = columnSample(this, i);
			samples = resolved;
		}
	}
	private static final class DiskCache {
		private static final ExecutorService IO = Executors.newFixedThreadPool(2, task -> {
			Thread thread = new Thread(task, "WitcherCraft world map cache I/O"); thread.setDaemon(true); return thread;
		});
		private static final ExecutorService WRITES = Executors.newSingleThreadExecutor(task -> {
			Thread thread = new Thread(task, "WitcherCraft world map cache writer"); thread.setDaemon(true); return thread;
		});
		private static final Set<Long> tiles = ConcurrentHashMap.newKeySet();
		private static final Set<Long> loadingTiles = ConcurrentHashMap.newKeySet();
		private static final Set<String> loadingRegions = ConcurrentHashMap.newKeySet();
		private static final Object[] regionFileLocks = new Object[64];
		private static volatile Path root;
		private static volatile boolean configured, ready;
		private static long serial, nextVisibleScanNanos;
		static { Arrays.setAll(regionFileLocks, ignored -> new Object()); }

		static synchronized void configure(UUID worldId, UUID playerId, long generation) {
			Path next = Minecraft.getInstance().gameDirectory.toPath().resolve("witchercraft-map-cache").resolve("v1")
				.resolve(worldId.toString()).resolve(playerId.toString()).resolve("overworld");
			if (configured && next.equals(root)) return;
			root = next; configured = true; ready = false; tiles.clear(); loadingTiles.clear(); loadingRegions.clear();
			long scanSerial = ++serial;
			IO.execute(() -> scan(next, scanSerial));
		}

		private static void scan(Path expectedRoot, long expectedSerial) {
			Set<Long> found = new HashSet<>();
			Path tileRoot = expectedRoot.resolve("tiles");
			if (Files.isDirectory(tileRoot)) try (var paths = Files.walk(tileRoot)) {
				paths.filter(Files::isRegularFile).forEach(path -> {
					String[] parts = path.getFileName().toString().split("\\.");
					if (parts.length != 4 || !parts[0].equals("c") || !parts[3].equals("wct")) return;
					try { found.add(ChunkPos.pack(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]))); }
					catch (NumberFormatException ignored) {}
				});
			} catch (IOException exception) {
				WitchercraftMod.LOGGER.warn("Cannot scan world map client cache {}", tileRoot, exception);
			}
			synchronized (DiskCache.class) {
				if (serial != expectedSerial || !Objects.equals(root, expectedRoot)) return;
				tiles.addAll(found); ready = true;
			}
			long files=0,bytes=0;
			if(Files.isDirectory(expectedRoot))try(var paths=Files.walk(expectedRoot)){for(Path path:paths.filter(Files::isRegularFile).toList()){files++;try{bytes+=Files.size(path);}catch(IOException ignored){}}}catch(IOException ignored){}
			WitchercraftMod.LOGGER.info("World map client cache ready: tiles={}, files={}, size_mib={}",found.size(),files,String.format(Locale.ROOT,"%.2f",bytes/1048576.0));
		}

		static boolean readyOrUnconfigured() { return !configured || ready; }
		static boolean hasTile(long key) { return configured && tiles.contains(key); }
		static boolean readyForOverview(int pageX,int pageZ) {
			if(!configured||!ready)return true;
			int firstX=pageX*OVERVIEW_CHUNKS,firstZ=pageZ*OVERVIEW_CHUNKS;
			for(int z=0;z<OVERVIEW_CHUNKS;z++)for(int x=0;x<OVERVIEW_CHUNKS;x++){
				long key=ChunkPos.pack(firstX+x,firstZ+z);if(tiles.contains(key)&&!TILES.containsKey(key))return false;
			}
			return true;
		}

		static void updateVisible(int minX, int maxX, int minZ, int maxZ, double centerX, double centerZ, long settingsKey, long generation, boolean overview) {
			Path currentRoot = root; if (!configured || !ready || currentRoot == null) return;
			long now=System.nanoTime();if(now<nextVisibleScanNanos)return;nextVisibleScanNanos=now+250_000_000L;
			int chunksPerPage=overview?OVERVIEW_CHUNKS:REGION_CHUNKS,prefetch=overview?OVERVIEW_PREFETCH:PREFETCH_REGIONS;
			int minRX=Math.floorDiv(minX,chunksPerPage)-prefetch,maxRX=Math.floorDiv(maxX,chunksPerPage)+prefetch,minRZ=Math.floorDiv(minZ,chunksPerPage)-prefetch,maxRZ=Math.floorDiv(maxZ,chunksPerPage)+prefetch;
			List<int[]> regions = new ArrayList<>();
			for(int z=minRZ;z<=maxRZ;z++)for(int x=minRX;x<=maxRX;x++)regions.add(new int[]{x,z});
			int pageBlocks=overview?OVERVIEW:REGION;
			regions.sort(Comparator.comparingDouble(p -> { double dx=(p[0]+0.5)*pageBlocks-centerX,dz=(p[1]+0.5)*pageBlocks-centerZ; return dx*dx+dz*dz; }));
			for(int[] region:regions) {
				Path path=overview?overviewPath(region[0],region[1],settingsKey):regionPath(region[0],region[1],settingsKey); String key=path.toString();
				if(overview){OverviewPage inMemory=OVERVIEWS.get(ChunkPos.pack(region[0],region[1]));if(inMemory!=null&&inMemory.texture!=null)continue;}
				else{ClientRegion inMemory=REGIONS.get(ChunkPos.pack(region[0],region[1]));if(inMemory!=null&&inMemory.textures[0]!=null)continue;}
				if(!Files.isRegularFile(path)||!loadingRegions.add(key))continue;
				if(overview)IO.execute(() -> loadOverview(path,region[0],region[1],settingsKey,generation,key));
				else IO.execute(() -> loadRegion(path,region[0],region[1],settingsKey,generation,key));
			}
			PriorityQueue<TileDistance> nearest=new PriorityQueue<>(Comparator.comparingDouble(TileDistance::distanceSquared).reversed());
			int prefetchMinX=minRX*chunksPerPage,prefetchMaxX=(maxRX+1)*chunksPerPage-1,prefetchMinZ=minRZ*chunksPerPage,prefetchMaxZ=(maxRZ+1)*chunksPerPage-1;
			for(int z=prefetchMinZ;z<=prefetchMaxZ;z++)for(int x=prefetchMinX;x<=prefetchMaxX;x++){
				long key=ChunkPos.pack(x,z); if(!tiles.contains(key)||TILES.containsKey(key)||loadingTiles.contains(key))continue;
				double dx=x*CHUNK+8-centerX,dz=z*CHUNK+8-centerZ;nearest.add(new TileDistance(key,dx*dx+dz*dz));if(nearest.size()>256)nearest.poll();
			}
			for(TileDistance candidate:nearest)if(loadingTiles.add(candidate.position))IO.execute(() -> loadTile(currentRoot,candidate.position,generation));
		}

		private static void loadRegion(Path path,int x,int z,long settingsKey,long generation,String loadingKey) {
			try {
				synchronized(regionFileLock(path)) {
					long[] savedCoverage=readCoverage(path);
					long[] currentCoverage=coverageFromIndex(x,z);
					boolean stale=savedCoverage==null||!Arrays.equals(savedCoverage,currentCoverage);
					try(InputStream input=Files.newInputStream(path)){
						NativeImage image=NativeImage.read(input);
						if(image.getWidth()!=REGION||image.getHeight()!=REGION){image.close();throw new IOException("Unexpected world-map leaf size");}
						CACHED_REGIONS.add(new CachedRegion(x,z,settingsKey,generation,stale,loadingKey,image));
					}
					if(stale)STALE_CACHED_REGIONS.add(ChunkPos.pack(x,z));
				}
			}
			catch(IOException|RuntimeException exception){ loadingRegions.remove(loadingKey);try{Files.deleteIfExists(path);}catch(IOException ignored){} }
		}

		static void finishedRegionLoad(String loadingKey){loadingRegions.remove(loadingKey);}

		private static void loadOverview(Path path,int x,int z,long settingsKey,long generation,String loadingKey) {
			try {
				synchronized(regionFileLock(path)) {
					long[] savedCoverage=readCoverage(path),currentCoverage=coverageFromIndex(x,z,OVERVIEW_CHUNKS);
					boolean stale=savedCoverage==null||!Arrays.equals(savedCoverage,currentCoverage);
					try(InputStream input=Files.newInputStream(path)){
						NativeImage image=NativeImage.read(input);
						if(image.getWidth()!=OVERVIEW_PIXELS||image.getHeight()!=OVERVIEW_PIXELS){image.close();throw new IOException("Unexpected world-map overview size");}
						CACHED_OVERVIEWS.add(new CachedOverview(x,z,settingsKey,generation,stale,loadingKey,image));
					}
				}
			}
			catch(IOException|RuntimeException exception){loadingRegions.remove(loadingKey);try{Files.deleteIfExists(path);}catch(IOException ignored){}}
		}

		static void finishedOverviewLoad(String loadingKey){loadingRegions.remove(loadingKey);}

		private static void loadTile(Path expectedRoot,long key,long generation) {
			ChunkPos pos=ChunkPos.unpack(key);
			Path path=tilePath(expectedRoot,pos.x(),pos.z());
			boolean queued=false;
			try { Optional<WorldMapTerrainTile> loaded=WorldMapTerrainTile.read(path,pos.x(),pos.z());
				if(loaded.isPresent()){CACHED_TILES.add(new CachedTile(key,loaded.get(),generation));queued=true;}
				else{tiles.remove(key);try{Files.deleteIfExists(path);}catch(IOException ignored){}CACHE_MISSES.add(key);}
			}
			finally { if(!queued)loadingTiles.remove(key); }
		}

		static void finishedTileLoad(long position){loadingTiles.remove(position);}

		static void saveTile(WorldMapTerrainTile tile) {
			Path currentRoot=root;if(!configured||currentRoot==null)return; long key=ChunkPos.pack(tile.chunkX(),tile.chunkZ());tiles.add(key);
			WRITES.execute(() -> { try{tile.writeAtomically(tilePath(currentRoot,tile.chunkX(),tile.chunkZ()));}catch(IOException exception){WitchercraftMod.LOGGER.warn("Cannot save world map client tile {},{}",tile.chunkX(),tile.chunkZ(),exception);} });
		}

		static Path regionPath(int x,int z,long settingsKey) {
			Path currentRoot=root; return currentRoot==null?null:currentRoot.resolve("leaves-v1").resolve("l."+x+"."+z+"."+Long.toUnsignedString(settingsKey,16)+".png");
		}
		static Path overviewPath(int x,int z,long settingsKey) {
			Path currentRoot=root; return currentRoot==null?null:currentRoot.resolve("overviews-v2").resolve("o."+x+"."+z+"."+Long.toUnsignedString(settingsKey,16)+".png");
		}

		static void writeRegion(Path target,NativeImage image,long[] coverage) {
			synchronized(regionFileLock(target)) { try { Files.createDirectories(target.getParent()); Path temporary=target.resolveSibling(target.getFileName()+".tmp-"+UUID.randomUUID()); image.writeToFile(temporary);
				try{Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ignored){Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING);}
				Path coverageTarget=coveragePath(target),coverageTemporary=coverageTarget.resolveSibling(coverageTarget.getFileName()+".tmp-"+UUID.randomUUID());
				try(DataOutputStream output=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(coverageTemporary)))){output.writeInt(0x57434D43);output.writeInt(2);for(int i=0;i<4;i++)output.writeLong(i<coverage.length?coverage[i]:0L);}
				try{Files.move(coverageTemporary,coverageTarget,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ignored){Files.move(coverageTemporary,coverageTarget,StandardCopyOption.REPLACE_EXISTING);} }
			catch(IOException exception){WitchercraftMod.LOGGER.warn("Cannot save world map client region {}",target,exception);} }
		}

		private static Object regionFileLock(Path image){return regionFileLocks[Math.floorMod(image.toAbsolutePath().normalize().hashCode(),regionFileLocks.length)];}
		private static Path coveragePath(Path image){return image.resolveSibling(image.getFileName()+".coverage");}
		private static long[] readCoverage(Path image)throws IOException{
			Path path=coveragePath(image);if(!Files.isRegularFile(path))return null;
			try(DataInputStream input=new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))){
				if(input.readInt()!=0x57434D43||input.readInt()!=2)return null;long[] result=new long[4];for(int i=0;i<4;i++)result[i]=input.readLong();return result;
			}
		}
		private static long[] coverageFromIndex(int regionX,int regionZ){
			return coverageFromIndex(regionX,regionZ,REGION_CHUNKS);
		}
		private static long[] coverageFromIndex(int regionX,int regionZ,int chunksPerSide){
			BitSet result=new BitSet(chunksPerSide*chunksPerSide);int firstX=regionX*chunksPerSide,firstZ=regionZ*chunksPerSide;
			for(int z=0;z<chunksPerSide;z++)for(int x=0;x<chunksPerSide;x++)if(tiles.contains(ChunkPos.pack(firstX+x,firstZ+z)))result.set(x+z*chunksPerSide);
			return Arrays.copyOf(result.toLongArray(),4);
		}

		private static Path tilePath(Path base,int x,int z){return base.resolve("tiles").resolve("r."+(x>>5)+"."+(z>>5)).resolve("c."+x+"."+z+".wct");}
		static synchronized void clear(){root=null;configured=false;ready=false;tiles.clear();loadingTiles.clear();loadingRegions.clear();nextVisibleScanNanos=0;serial++;}
	}
	private record RegionTexture(Identifier id) {}
	private static final class OverviewPage {
		final int x, z;
		long revision = 1, builtRevision, queuedRevision, lastUsedFrame = Long.MIN_VALUE;
		RegionTexture texture;
		OverviewPage(int x, int z) { this.x = x; this.z = z; }
		boolean needsBuild() { return builtRevision != revision && queuedRevision != revision; }
		double distanceSquared(double centerX, double centerZ) { double dx = (x + 0.5) * OVERVIEW - centerX, dz = (z + 0.5) * OVERVIEW - centerZ; return dx * dx + dz * dz; }
		void retireTexture() { if (texture != null) { WorldMapClientTileCache.retireTexture(texture.id); texture = null; } }
	}
	private static final class ClientRegion {
		final int x, z; final BitSet presentChunks = new BitSet(REGION_CHUNKS * REGION_CHUNKS);
		final RegionTexture[] textures = new RegionTexture[LOD_COUNT]; final long[] builtRevisions = new long[LOD_COUNT], queuedRevisions = new long[LOD_COUNT]; long revision = 1, lastUsedFrame = Long.MIN_VALUE;
		ClientRegion(int x, int z) { this.x = x; this.z = z; }
		void setPresent(int chunkX, int chunkZ, boolean present) {
			presentChunks.set(Math.floorMod(chunkX, REGION_CHUNKS) + Math.floorMod(chunkZ, REGION_CHUNKS) * REGION_CHUNKS, present);
		}
		boolean needsBuild(int lod) { return builtRevisions[lod] != revision && queuedRevisions[lod] != revision; }
		RegionTexture current(int lod) { return textures[lod]; }
		double distanceSquared(double centerX, double centerZ) { double dx=(x+0.5)*REGION-centerX,dz=(z+0.5)*REGION-centerZ; return dx*dx+dz*dz; }
		long gpuBytes() { long n = 0; for (int i = 0; i < LOD_COUNT; i++) if (textures[i] != null) { long s = REGION >> i; n += s * s * 4; } return n; }
		void retireTextures() { for (int i = 0; i < LOD_COUNT; i++) if (textures[i] != null) { retireTexture(textures[i].id); textures[i] = null; } }
	}
}

package net.redboltmedia.witchercraft;

import java.util.*;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/** Bounded decoded-tile and region-texture caches for authorized world-map terrain. */
@EventBusSubscriber(Dist.CLIENT)
public final class WorldMapClientTileCache {
	private static final int CHUNK = 16, REGION_CHUNKS = 16, REGION = 256, LOD_COUNT = 1;
	private static final int MAX_TILES = 2048, MAX_REGIONS = 64, BATCHES_PER_VIEW = 2;
	private static final long RETRY_NANOS = 5_000_000_000L, DIAGNOSTIC_NANOS = 5_000_000_000L;
	private static final double[] REGRESSION_ZOOMS = { 0.25, 1.0, 1.37, 5.24, 16.0 };
	private static final LinkedHashMap<Long, DecodedTile> TILES = new LinkedHashMap<>(256, .75f, true);
	private static final LinkedHashMap<Long, ClientRegion> REGIONS = new LinkedHashMap<>(32, .75f, true);
	private static final Map<Long, Long> REQUESTED = new HashMap<>();
	private static final Map<String, Integer> BLOCK_TEXTURE_COLORS = new HashMap<>();
	private static final Set<String> FAILED_BLOCK_TEXTURE_COLORS = new HashSet<>();
	private static final ArrayDeque<RetiredTexture> RETIRED_TEXTURES = new ArrayDeque<>();
	private static Object connectionIdentity;
	private static int nextRequestId = 1, inFlightRequestId, batchesForView, selectedLod;
	private static long viewGeneration, inFlightGeneration, renderFrame, textureSequence;
	private static boolean viewDirty = true, haveViewBounds;
	private static int lastMinChunkX, lastMaxChunkX, lastMinChunkZ, lastMaxChunkZ, lastViewportW, lastViewportH;
	private static double lastCenterX, lastCenterZ, lastZoom;
	private static long rebuilds, uploads, draws, frames, renderNanos, lastDiagnostic;
	private static long visualSettingsKey;

	static { validateRegressionGeometry(); }
	private WorldMapClientTileCache() {}
	@SubscribeEvent
	public static void registerReloadListener(AddClientReloadListenersEvent event) {
		event.addListener(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_block_colors"), (ResourceManagerReloadListener) manager -> {
			BLOCK_TEXTURE_COLORS.clear();
			FAILED_BLOCK_TEXTURE_COLORS.clear();
			for (ClientRegion region : REGIONS.values()) region.revision++;
		});
	}

	public static void renderAndRequest(GuiGraphicsExtractor g, int vx, int vy, int vw, int vh, double centerX, double centerZ, double zoom) {
		long started = System.nanoTime();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
		renderFrame++;
		releaseRetiredTextures(mc);
		if (connectionIdentity != mc.getConnection()) { clear(); connectionIdentity = mc.getConnection(); }
		rememberView(vw, vh, centerX, centerZ, zoom);
		refreshVisualSettings();
		selectedLod = 0;
		double originX = vx + vw / 2.0 - centerX * zoom;
		double originY = vy + vh / 2.0 - centerZ * zoom;
		int minRX = Math.floorDiv(lastMinChunkX, REGION_CHUNKS), maxRX = Math.floorDiv(lastMaxChunkX, REGION_CHUNKS);
		int minRZ = Math.floorDiv(lastMinChunkZ, REGION_CHUNKS), maxRZ = Math.floorDiv(lastMaxChunkZ, REGION_CHUNKS);
		for (int rz = minRZ; rz <= maxRZ; rz++) for (int rx = minRX; rx <= maxRX; rx++) {
			long key = ChunkPos.pack(rx, rz);
			ClientRegion region = REGIONS.get(key);
			if (region == null) { region = createRegion(rx, rz); REGIONS.put(key, region); }
			if (region.needsBuild(selectedLod)) buildTexture(region, selectedLod);
			RegionTexture texture = region.current(selectedLod);
			if (texture == null) continue;
			int worldX = rx * REGION, worldZ = rz * REGION;
			int left = edge(originX, worldX, zoom), top = edge(originY, worldZ, zoom);
			int right = edge(originX, worldX + REGION, zoom), bottom = edge(originY, worldZ + REGION, zoom);
			if (right <= vx || bottom <= vy || left >= vx + vw || top >= vy + vh) continue;
			region.lastUsedFrame = renderFrame;
			int sourceSize = REGION >> selectedLod;
			g.blit(RenderPipelines.GUI_TEXTURED, texture.id, left, top, 0, 0,
				Math.max(1, right - left), Math.max(1, bottom - top), sourceSize, sourceSize, sourceSize, sourceSize);
			draws++;
		}
		trimRegions();
		if (viewDirty && inFlightRequestId == 0) startRequest();
		frames++; renderNanos += System.nanoTime() - started; logDiagnostics();
	}

	public static boolean canPause() { return !viewDirty && inFlightRequestId == 0; }
	public static void markViewDirty() { viewDirty = true; viewGeneration++; batchesForView = 0; }

	public static void completeRequest(int requestId) {
		if (requestId != inFlightRequestId) return;
		inFlightRequestId = 0;
		batchesForView = inFlightGeneration == viewGeneration ? batchesForView + 1 : 0;
		if (batchesForView < BATCHES_PER_VIEW && startRequest()) return;
		viewDirty = false;
	}

	public static void accept(WorldMapTileDataMessage message) {
		if (Minecraft.getInstance().getConnection() == null) return;
		long key = ChunkPos.pack(message.chunkX(), message.chunkZ());
		REQUESTED.remove(key); TILES.remove(key);
		TILES.put(key, new DecodedTile(message.chunkX(), message.chunkZ(), message.groundHeights().clone(), message.groundColors().clone(),
			message.groundTintKinds().clone(), message.groundTints().clone(), message.foliageHeights().clone(), message.foliageColors().clone(),
			message.foliageTintKinds().clone(), message.foliageTints().clone(), message.waterHeights().clone(), message.waterTints().clone(),
			message.decorationKinds().clone(), message.decorationColors().clone(), message.decorationTintKinds().clone(), message.decorationTints().clone(),
			message.blockStatePalette().clone(), message.groundStateIndices().clone(), message.foliageStateIndices().clone(), message.decorationStateIndices().clone()));
		regionForChunk(message.chunkX(), message.chunkZ()).setPresent(message.chunkX(), message.chunkZ(), true);
		invalidate(message.chunkX(), message.chunkZ()); trimTiles();
	}

	public static void clear() {
		for (ClientRegion region : REGIONS.values()) region.retireTextures();
		REGIONS.clear(); TILES.clear(); REQUESTED.clear();
		inFlightRequestId = 0; viewDirty = true; viewGeneration++; batchesForView = 0; haveViewBounds = false; selectedLod = 0;
	}

	private static RegionTexture buildTexture(ClientRegion region, int lod) {
		int scale = 1, size = REGION;
		NativeImage image = new NativeImage(size, size, false);
		boolean any = false;
		for (int pz = 0; pz < size; pz++) for (int px = 0; px < size; px++) {
			int wx = region.x * REGION + px * scale, wz = region.z * REGION + pz * scale;
			Sample sample = sampleAt(wx, wz);
			if (sample == null) { image.setPixelABGR(px, pz, 0); continue; }
			any = true;
			int[] heights = new int[9];
			int h = 0;
			for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
				Sample neighbor = sampleAt(wx + dx, wz + dz);
				heights[h++] = neighbor == null ? sample.height : neighbor.height;
			}
			image.setPixelABGR(px, pz, argbToAbgr(shade(sample.argb, heights, sample.water)));
		}
		Minecraft mc = Minecraft.getInstance();
		RegionTexture old = region.textures[lod];
		if (old != null) { retireTexture(old.id); region.textures[lod] = null; }
		region.builtRevisions[lod] = region.revision; rebuilds++;
		if (!any) { image.close(); return null; }
		Identifier id = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID,
			"world_map/region_" + region.x + "_" + region.z + "_lod_" + lod + "_v_" + textureSequence++);
		mc.getTextureManager().register(id, new DynamicTexture(() -> "WitcherCraft map region " + region.x + "," + region.z + " LOD " + lod, image));
		RegionTexture result = new RegionTexture(id); region.textures[lod] = result; uploads++;
		return result;
	}

	private static Sample sampleAt(int wx, int wz) {
		DecodedTile tile = tileAt(wx, wz);
		if (tile == null) return null;
		int index = Math.floorMod(wx, CHUNK) + Math.floorMod(wz, CHUNK) * CHUNK;
		int groundHeight = tile.groundHeights[index] == WorldMapTerrainTile.NO_HEIGHT ? 0 : tile.groundHeights[index];
		boolean water = tile.waterHeights[index] != WorldMapTerrainTile.NO_HEIGHT;
		return new Sample(columnColor(tile, index), groundHeight, water);
	}

	private static DecodedTile tileAt(int wx, int wz) {
		return TILES.get(ChunkPos.pack(Math.floorDiv(wx, CHUNK), Math.floorDiv(wz, CHUNK)));
	}

	private static int columnColor(DecodedTile tile, int index) {
		int ground = layerColor(tile, tile.groundColors[index], tile.groundTintKinds[index], tile.groundTints[index], tile.groundStateIndices[index]);
		if (tile.waterHeights[index] != WorldMapTerrainTile.NO_HEIGHT) {
			int depth = tile.groundHeights[index] == WorldMapTerrainTile.NO_HEIGHT ? 1 : Math.max(1, tile.waterHeights[index] - tile.groundHeights[index]);
			int water = scaleColor(0xFF000000 | tile.waterTints[index], 0.88);
			int color = ground == 0 ? water : mix(ground, water, 3, 4);
			double factor = Math.max(0.82, 1.01 - Math.min(32, depth) * 0.006);
			return scaleColor(color, factor);
		}
		if (tile.foliageHeights[index] != WorldMapTerrainTile.NO_HEIGHT) {
			int foliage = layerColor(tile, tile.foliageColors[index], tile.foliageTintKinds[index], tile.foliageTints[index], tile.foliageStateIndices[index]);
			if (foliage != 0) return ground == 0 ? foliage : mix(ground, foliage, 3, 4);
		}
		if (WorldMapClientConfig.showDecorations() && tile.decorationKinds[index] != 0) {
			int decoration = layerColor(tile, tile.decorationColors[index], tile.decorationTintKinds[index], tile.decorationTints[index], tile.decorationStateIndices[index]);
			if (decoration != 0) return ground == 0 ? decoration : mix(ground, decoration, 3, 4);
		}
		return ground;
	}

	private static int layerColor(DecodedTile tile, byte colorId, byte tintKind, int tint, short stateIndex) {
		int base = MapColor.byId(Byte.toUnsignedInt(colorId)).calculateARGBColor(MapColor.Brightness.NORMAL);
		boolean textureResolved = false;
		int paletteIndex = Short.toUnsignedInt(stateIndex);
		if (paletteIndex > 0 && paletteIndex < tile.blockStatePalette.length) {
			String state = tile.blockStatePalette[paletteIndex]; Integer resolved = BLOCK_TEXTURE_COLORS.get(state);
			if (resolved == null && !FAILED_BLOCK_TEXTURE_COLORS.contains(state)) {
				resolved = resolveBlockTextureColor(state);
				if (resolved == null) FAILED_BLOCK_TEXTURE_COLORS.add(state); else BLOCK_TEXTURE_COLORS.put(state, resolved);
			}
			if (resolved != null) { base = resolved; textureResolved = true; }
		}
		if (base == 0 || tintKind == 0) return base;
		if (!textureResolved) {
			int biome = scaleColor(0xFF000000 | tint, 0.82);
			return mix(base, biome, (int)Math.round(WorldMapClientConfig.biomeColorStrength() * 35.0), 100);
		}
		int tinted = multiply(base, 0xFF000000 | tint);
		return mix(base, tinted, (int)Math.round(WorldMapClientConfig.biomeColorStrength() * 100.0), 100);
	}
	private static Integer resolveBlockTextureColor(String serializedState) {
		try {
			BlockState state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, serializedState, false).blockState();
			BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
			List<BlockStateModelPart> parts = new ArrayList<>(); model.collectParts(RandomSource.create(serializedState.hashCode()), parts);
			BakedQuad selected = null; double selectedArea = -1.0;
			for (BlockStateModelPart part : parts) {
				for (BakedQuad quad : part.getQuads(Direction.UP)) { double area = quadArea(quad); if (area > selectedArea) { selected = quad; selectedArea = area; } }
				for (BakedQuad quad : part.getQuads(null)) if (quad.direction() == Direction.UP) { double area = quadArea(quad); if (area > selectedArea) { selected = quad; selectedArea = area; } }
			}
			TextureAtlasSprite sprite = selected == null ? model.particleMaterial().sprite() : selected.materialInfo().sprite();
			int color = averageSprite(sprite); return color == 0 ? null : color;
		} catch (CommandSyntaxException | RuntimeException exception) {
			return null;
		}
	}
	private static double quadArea(BakedQuad quad) {
		double ax=quad.position1().x()-quad.position0().x(), ay=quad.position1().y()-quad.position0().y(), az=quad.position1().z()-quad.position0().z();
		double bx=quad.position2().x()-quad.position0().x(), by=quad.position2().y()-quad.position0().y(), bz=quad.position2().z()-quad.position0().z();
		double cx=ay*bz-az*by, cy=az*bx-ax*bz, cz=ax*by-ay*bx;
		return Math.sqrt(cx*cx+cy*cy+cz*cz) * 0.5;
	}
	private static int averageSprite(TextureAtlasSprite sprite) {
		NativeImage image = sprite.contents().getOriginalImage(); long r=0,g=0,b=0,weight=0;
		for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
			int pixel=image.getPixel(x,y),alpha=pixel>>>24;if(alpha<=10)continue;
			r+=(long)(pixel>>16&255)*alpha;g+=(long)(pixel>>8&255)*alpha;b+=(long)(pixel&255)*alpha;weight+=alpha;
		}
		return weight==0?0:0xFF000000|(int)(r/weight)<<16|(int)(g/weight)<<8|(int)(b/weight);
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
	private static int scaleColor(int color, double factor) {
		return 0xFF000000 | Math.min(255,(int)((color>>16&255)*factor))<<16 | Math.min(255,(int)((color>>8&255)*factor))<<8 | Math.min(255,(int)((color&255)*factor));
	}

	private static int shade(int argb, int[] h, boolean water) {
		if (argb == 0) return 0;
		int directionalSlope = Math.max(-6, Math.min(6, (h[3] - h[5]) + (h[1] - h[7])));
		double strength = WorldMapClientConfig.hillshadeStrength() * (water ? 0.55 : 1.0);
		double factor = 1.0 + directionalSlope * 0.028 * strength;
		factor = Math.max(0.72, Math.min(1.28, factor)) * WorldMapClientConfig.terrainBrightness();
		return scaleColor(argb, factor);
	}
	private static void refreshVisualSettings() {
		long key = WorldMapClientConfig.showDecorations() ? 1 : 0;
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.terrainBrightness());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.biomeColorStrength());
		key = 31 * key + Double.doubleToLongBits(WorldMapClientConfig.hillshadeStrength());
		if (key == visualSettingsKey) return;
		visualSettingsKey = key;
		for (ClientRegion region : REGIONS.values()) region.revision++;
	}

	private static int edge(double origin, int world, double zoom) { return (int)Math.round(origin + world * zoom); }
	private static void validateRegressionGeometry() {
		for (double zoom : REGRESSION_ZOOMS) {
			if (edge(0, REGION, zoom) - edge(0, 0, zoom) != Math.round(REGION * zoom))
				throw new IllegalStateException("World map scaling regression at " + zoom);
			for (int lod = 0; lod < LOD_COUNT; lod++) if ((REGION >> lod) * (1 << lod) != REGION)
				throw new IllegalStateException("World map LOD coverage regression");
		}
	}

	private static void invalidate(int chunkX, int chunkZ) {
		int rx = Math.floorDiv(chunkX, REGION_CHUNKS), rz = Math.floorDiv(chunkZ, REGION_CHUNKS);
		dirty(rx, rz);
		if (Math.floorMod(chunkX, REGION_CHUNKS) == REGION_CHUNKS - 1) dirty(rx + 1, rz);
		if (Math.floorMod(chunkZ, REGION_CHUNKS) == REGION_CHUNKS - 1) dirty(rx, rz + 1);
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
		long now = System.nanoTime();
		PriorityQueue<TileDistance> nearest = new PriorityQueue<>(Comparator.comparingDouble(TileDistance::distanceSquared).reversed());
		for (int z = lastMinChunkZ; z <= lastMaxChunkZ; z++) for (int x = lastMinChunkX; x <= lastMaxChunkX; x++) {
			long key = ChunkPos.pack(x, z); if (TILES.containsKey(key)) continue;
			Long at = REQUESTED.get(key); if (at != null && now - at < RETRY_NANOS && now >= at) continue;
			double dx = x * CHUNK + 8 - lastCenterX, dz = z * CHUNK + 8 - lastCenterZ;
			nearest.add(new TileDistance(key, dx * dx + dz * dz));
			if (nearest.size() > WorldMapTileRequestMessage.MAX_POSITIONS) nearest.poll();
		}
		if (nearest.isEmpty()) return false;
		List<TileDistance> ordered = new ArrayList<>(nearest); ordered.sort(Comparator.comparingDouble(TileDistance::distanceSquared));
		long[] positions = new long[ordered.size()];
		for (int i = 0; i < ordered.size(); i++) { positions[i] = ordered.get(i).position; REQUESTED.put(positions[i], now); }
		int id = nextRequestId++; if (nextRequestId <= 0) nextRequestId = 1;
		inFlightRequestId = id; inFlightGeneration = viewGeneration;
		ClientPacketDistributor.sendToServer(new WorldMapTileRequestMessage(id, positions)); return true;
	}

	private static void trimTiles() {
		Iterator<DecodedTile> it = TILES.values().iterator();
		while (TILES.size() > MAX_TILES && it.hasNext()) {
			DecodedTile tile = it.next(); it.remove();
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
		long cpu = TILES.size() * 256L * 29, gpu = 0; for (ClientRegion r : REGIONS.values()) gpu += r.gpuBytes();
		double ms = frames == 0 ? 0 : renderNanos / 1_000_000.0 / frames;
		WitchercraftMod.LOGGER.debug("World map renderer: decoded_tiles={}, cpu_kib={}, gpu_regions={}, gpu_kib={}, lod={}, rebuilds={}, uploads={}, draw_calls={}, avg_render_ms={}",
			TILES.size(), cpu / 1024, REGIONS.size(), gpu / 1024, selectedLod, rebuilds, uploads, draws, String.format(Locale.ROOT, "%.3f", ms));
		rebuilds = uploads = draws = frames = renderNanos = 0; lastDiagnostic = now;
	}
	private static int argbToAbgr(int c) { return c & 0xFF00FF00 | (c & 0x00FF0000) >> 16 | (c & 0xFF) << 16; }

	private record TileDistance(long position, double distanceSquared) {}
	private record Sample(int argb, int height, boolean water) {}
	private record RetiredTexture(Identifier id, long releaseFrame) {}
	private record DecodedTile(int chunkX, int chunkZ, short[] groundHeights, byte[] groundColors, byte[] groundTintKinds, int[] groundTints,
		short[] foliageHeights, byte[] foliageColors, byte[] foliageTintKinds, int[] foliageTints, short[] waterHeights, int[] waterTints,
		byte[] decorationKinds, byte[] decorationColors, byte[] decorationTintKinds, int[] decorationTints,
		String[] blockStatePalette, short[] groundStateIndices, short[] foliageStateIndices, short[] decorationStateIndices) {}
	private record RegionTexture(Identifier id) {}
	private static final class ClientRegion {
		final int x, z; final BitSet presentChunks = new BitSet(REGION_CHUNKS * REGION_CHUNKS);
		final RegionTexture[] textures = new RegionTexture[LOD_COUNT]; final long[] builtRevisions = new long[LOD_COUNT]; long revision = 1, lastUsedFrame = Long.MIN_VALUE;
		ClientRegion(int x, int z) { this.x = x; this.z = z; }
		void setPresent(int chunkX, int chunkZ, boolean present) {
			presentChunks.set(Math.floorMod(chunkX, REGION_CHUNKS) + Math.floorMod(chunkZ, REGION_CHUNKS) * REGION_CHUNKS, present);
		}
		boolean needsBuild(int lod) { return builtRevisions[lod] != revision; }
		RegionTexture current(int lod) { return builtRevisions[lod] == revision ? textures[lod] : null; }
		long gpuBytes() { long n = 0; for (int i = 0; i < LOD_COUNT; i++) if (textures[i] != null) { long s = REGION >> i; n += s * s * 4; } return n; }
		void retireTextures() { for (int i = 0; i < LOD_COUNT; i++) if (textures[i] != null) { retireTexture(textures[i].id); textures[i] = null; } }
	}
}

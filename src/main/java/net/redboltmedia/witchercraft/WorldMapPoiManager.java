package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jspecify.annotations.Nullable;

import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Persistent POI observation, spatial indexing, reveal, and discovery owner. */
@EventBusSubscriber
public final class WorldMapPoiManager {
	private static final int REVEAL_INTERVAL_TICKS = 200;
	private static final int DISCOVERY_INTERVAL_TICKS = 20;
	private static final int DIAGNOSTIC_INTERVAL_TICKS = 1200;
	private static final int MAX_MARKERS_PER_REQUEST = 4096;
	private static final Map<MinecraftServer, ServerState> SERVERS = new ConcurrentHashMap<>();

	private WorldMapPoiManager() {
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event) {
		if (!WorldMapServerConfig.poisEnabled() || !event.getLevel().dimension().equals(Level.OVERWORLD))
			return;
		ServerState state = SERVERS.computeIfAbsent(event.getLevel().getServer(), ServerState::new);
		state.reconcileLoadedChunk(event.getLevel(), event.getChunk());
		WorldMapPoiDefinitions.Snapshot definitions = WorldMapPoiDefinitions.active();
		if (definitions.definitions().isEmpty())
			return;
		state.watchedChunks++;
		definitions.providers().observeLoadedChunk(new WorldMapPoiProvider.ObservationContext(event.getLevel(), event.getChunk()),
			instance -> state.accept(instance, definitions));
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (WorldMapServerConfig.poisEnabled())
			SERVERS.computeIfAbsent(event.getServer(), ServerState::new).tick(WorldMapPoiDefinitions.active());
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		SERVERS.remove(event.getServer());
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ServerState state = SERVERS.computeIfAbsent(player.level().getServer(), ServerState::new);
			WorldMapPoiDefinitions.Snapshot definitions = WorldMapPoiDefinitions.active();
			if (state.sharing)
				state.syncShared(player, definitions);
			state.sendReset(player, definitions);
		}
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ServerState state = SERVERS.get(player.level().getServer());
			if (state != null)
				state.requestAllowances.remove(player.getUUID());
		}
	}

	public static void onDefinitionsReloaded(WorldMapPoiDefinitions.Snapshot definitions) {
		for (ServerState state : SERVERS.values()) {
			state.reconcile(definitions);
			for (ServerPlayer player : state.server.getPlayerList().getPlayers())
				state.sendReset(player, definitions);
		}
	}

	public static void requestMarkers(ServerPlayer player, WorldMapPoiViewRequestMessage request) {
		if (!WorldMapServerConfig.poisEnabled()) {
			PacketDistributor.sendToPlayer(player, new WorldMapPoiRequestCompleteMessage(request.requestId(), true,
				WorldMapWorldIdentity.get(player.level().getServer()), WorldMapPoiDefinitions.active().generation(), request.cells()));
			return;
		}
		ServerState state = SERVERS.computeIfAbsent(player.level().getServer(), ServerState::new);
		state.requestMarkers(player, request, WorldMapPoiDefinitions.active());
	}

	/**
	 * Stores or replaces a lifecycle-managed instance created by a world event, such as a placed
	 * fast-travel sign. The record stays inactive while its definition is unavailable. A record stored
	 * with {@code discoverable} false is kept out of the spatial index, so nobody can reveal or discover
	 * it, until {@link #makeDiscoverable} is called. That hold is runtime-only and ends on restart.
	 * Server thread only.
	 */
	public static boolean putLifecycleInstance(MinecraftServer server, WorldMapPoiInstance instance, boolean discoverable) {
		return SERVERS.computeIfAbsent(server, ServerState::new).putLifecycle(instance, discoverable);
	}

	public static void makeDiscoverable(MinecraftServer server, UUID markerId) {
		SERVERS.computeIfAbsent(server, ServerState::new).makeDiscoverable(markerId);
	}

	/**
	 * Discovers a POI for one player immediately, skipping the reveal pass and discovery radius, with the
	 * normal message and sound. Used when a player places a fast-travel sign. No-op for an inactive,
	 * held, unknown, or already discovered POI.
	 */
	public static void discoverFor(ServerPlayer player, UUID markerId) {
		ServerState state = SERVERS.computeIfAbsent(player.level().getServer(), ServerState::new);
		WorldMapPoiInstance instance = state.instances.get(markerId);
		WorldMapPoiDefinitions.Snapshot definitions = WorldMapPoiDefinitions.active();
		if (instance == null || !instance.active() || state.undiscoverable.contains(markerId) || !definitions.accepts(instance))
			return;
		WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
		if (definition != null && definition.discoveryRequired())
			state.discover(player, instance, definition, definitions.generation());
	}

	/**
	 * The active, accepted instance a player has discovered under the presentation UUID their client
	 * knows, counting shared discoveries while sharing is on. Null for anything else, so a client can only
	 * ever name a POI it was shown as discovered.
	 */
	public static @Nullable WorldMapPoiInstance discoveredByPresentation(ServerPlayer player, UUID presentationId) {
		ServerState state = SERVERS.computeIfAbsent(player.level().getServer(), ServerState::new);
		WorldMapPoiDefinitions.Snapshot definitions = WorldMapPoiDefinitions.active();
		for (WorldMapPoiKnowledge.Entry stored : state.knowledge.entries(player.getUUID())) {
			if (!stored.presentationId().equals(presentationId))
				continue;
			WorldMapPoiKnowledge.Entry entry = WorldMapPoiKnowledge.effective(stored, state.sharing);
			WorldMapPoiInstance instance = state.instances.get(stored.markerId());
			if (entry == null || entry.state() != WorldMapPoiKnowledge.State.DISCOVERED || instance == null || !instance.active()
				|| !definitions.accepts(instance))
				return null;
			return instance;
		}
		return null;
	}

	/** The presentation UUID a player's client uses for a marker, or null when the player does not know it. */
	public static @Nullable UUID presentationId(ServerPlayer player, UUID markerId) {
		ServerState state = SERVERS.computeIfAbsent(player.level().getServer(), ServerState::new);
		WorldMapPoiKnowledge.Entry entry = state.known(player.getUUID(), markerId);
		return entry == null ? null : entry.presentationId();
	}

	public static @Nullable WorldMapPoiInstance instance(MinecraftServer server, UUID markerId) {
		return SERVERS.computeIfAbsent(server, ServerState::new).instances.get(markerId);
	}

	/** The lifecycle-managed instance anchored at an exact block, if any. */
	public static @Nullable WorldMapPoiInstance lifecycleInstanceAt(MinecraftServer server, Identifier dimension, BlockPos anchor) {
		ServerState state = SERVERS.computeIfAbsent(server, ServerState::new);
		UUID markerId = state.lifecycleAnchors.get(new ServerState.AnchorKey(dimension, anchor.asLong()));
		return markerId == null ? null : state.instances.get(markerId);
	}

	/** Deletes an instance and every player's knowledge of it, and drops it from connected clients. */
	public static boolean removeInstance(MinecraftServer server, UUID markerId) {
		return SERVERS.computeIfAbsent(server, ServerState::new).remove(markerId);
	}

	/** Changes a custom name and refreshes the marker for connected players who know it. */
	public static boolean renameInstance(MinecraftServer server, UUID markerId, String customName) {
		return SERVERS.computeIfAbsent(server, ServerState::new).rename(markerId, customName);
	}

	public static int countInstances(MinecraftServer server, Identifier providerType, Identifier sourceId) {
		int count = 0;
		for (WorldMapPoiInstance instance : SERVERS.computeIfAbsent(server, ServerState::new).instances.values())
			if (instance.providerType().equals(providerType) && instance.sourceId().equals(sourceId))
				count++;
		return count;
	}

	public static Collection<WorldMapPoiInstance> instances(MinecraftServer server) {
		ServerState state = SERVERS.get(server);
		return state == null ? java.util.List.of() : state.instances.values();
	}

	private static final class ServerState {
		private final MinecraftServer server;
		private final WorldMapPoiInstances instances;
		private final WorldMapPoiKnowledge knowledge;
		private final WorldMapPoiSpatialIndex spatialIndex = new WorldMapPoiSpatialIndex();
		private final Map<UUID, RequestAllowance> requestAllowances = new HashMap<>();
		private final Map<AnchorKey, UUID> lifecycleAnchors = new HashMap<>();
		private final Map<ChunkKey, Set<UUID>> lifecycleByChunk = new HashMap<>();
		private final Set<UUID> undiscoverable = new HashSet<>();
		/** Last seen value of the shared signpost discovery setting, to react when it changes. */
		private boolean sharing;
		private double maximumRevealRadius;
		private double maximumDiscoveryRadius;
		private long watchedChunks;
		private long matchedObservations;
		private long uniqueInstances;
		private long refreshedInstances;
		private long duplicateObservations;
		private long observationCollisions;
		private long reveals;
		private long discoveries;
		private long viewRequests;
		private long rejectedViewRequests;
		private long timedTicks;
		private long totalTickNanos;
		private long maximumTickNanos;
		private long revealCandidates;
		private long discoveryCandidates;
		private long lifecycleRemovals;
		private long staleRemovals;

		private ServerState(MinecraftServer server) {
			this.server = server;
			instances = WorldMapPoiInstances.get(server);
			knowledge = WorldMapPoiKnowledge.get(server);
			sharing = WorldMapServerConfig.sharedSignDiscovery();
			for (WorldMapPoiInstance instance : instances.values())
				if (lifecycleManaged(instance))
					indexLifecycle(instance);
			reconcile(WorldMapPoiDefinitions.active());
		}

		private boolean putLifecycle(WorldMapPoiInstance instance, boolean discoverable) {
			WorldMapPoiInstance stored = instance.withActive(WorldMapPoiDefinitions.active().accepts(instance));
			WorldMapPoiInstances.ObservationResult result = instances.observe(stored);
			switch (result) {
				case CREATED, UPDATED, UNCHANGED -> {
					indexLifecycle(stored);
					if (discoverable) {
						undiscoverable.remove(stored.markerId());
						spatialIndex.upsert(stored);
					} else {
						undiscoverable.add(stored.markerId());
						spatialIndex.remove(stored.markerId());
					}
					if (result == WorldMapPoiInstances.ObservationResult.CREATED)
						WitchercraftMod.LOGGER.info("Registered world-map POI: marker={}, definition={}, source={}, dimension={}, anchor=[{}, {}, {}], active={}",
							stored.markerId(), stored.definitionId(), stored.sourceId(), stored.dimension(), stored.anchor().getX(),
							stored.anchor().getY(), stored.anchor().getZ(), stored.active());
					return true;
				}
				case COLLISION -> WitchercraftMod.LOGGER.error("Stable POI marker collision for {} and identity '{}'", stored.markerId(), stored.providerIdentity());
				case LIMIT_REACHED -> WitchercraftMod.LOGGER.error("Cannot retain POI {}: shared instance limit {} reached", stored.markerId(), WorldMapPoiInstances.MAX_INSTANCES);
			}
			return false;
		}

		private void makeDiscoverable(UUID markerId) {
			if (!undiscoverable.remove(markerId))
				return;
			WorldMapPoiInstance instance = instances.get(markerId);
			if (instance != null)
				spatialIndex.upsert(instance);
		}

		private boolean remove(UUID markerId) {
			WorldMapPoiInstance removed = instances.remove(markerId);
			if (removed == null)
				return false;
			spatialIndex.remove(markerId);
			undiscoverable.remove(markerId);
			unindexLifecycle(removed);
			lifecycleRemovals++;
			for (Map.Entry<UUID, UUID> forgotten : knowledge.forget(markerId).entrySet()) {
				ServerPlayer player = server.getPlayerList().getPlayer(forgotten.getKey());
				if (player != null)
					PacketDistributor.sendToPlayer(player, new WorldMapPoiRemovedMessage(removed.dimension(), forgotten.getValue()));
			}
			WitchercraftMod.LOGGER.info("Removed world-map POI: marker={}, definition={}, dimension={}, anchor=[{}, {}, {}]", markerId,
				removed.definitionId(), removed.dimension(), removed.anchor().getX(), removed.anchor().getY(), removed.anchor().getZ());
			return true;
		}

		private boolean rename(UUID markerId, String customName) {
			if (!instances.setCustomName(markerId, customName))
				return false;
			WorldMapPoiInstance instance = instances.get(markerId);
			WorldMapPoiDefinitions.Snapshot definitions = WorldMapPoiDefinitions.active();
			WorldMapPoiDefinition definition = instance == null ? null : definitions.definitions().get(instance.definitionId());
			if (definition == null || !instance.active() || !definitions.accepts(instance))
				return true;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				WorldMapPoiKnowledge.Entry entry = known(player.getUUID(), markerId);
				if (entry != null)
					pushMarker(player, instance, definition, entry, definitions.generation());
			}
			return true;
		}

		/** A player's knowledge of a POI as seen through the current shared discovery setting. */
		private WorldMapPoiKnowledge.@Nullable Entry known(UUID playerId, UUID markerId) {
			return WorldMapPoiKnowledge.effective(knowledge.get(playerId, markerId), sharing);
		}

		private static boolean sharesDiscovery(WorldMapPoiInstance instance) {
			return WorldMapPoiProviders.provider(instance.providerType()).map(WorldMapPoiProvider::sharesDiscovery).orElse(false);
		}

		/**
		 * Records a discovery of a shareable POI for the world. The first time, while sharing is on, every
		 * other online player receives it too, with the discovery message but no sound.
		 */
		private void shareDiscovery(ServerPlayer discoverer, WorldMapPoiInstance instance, WorldMapPoiDefinition definition, long generation) {
			if (!sharesDiscovery(instance) || !knowledge.markShared(instance.markerId()) || !sharing)
				return;
			for (ServerPlayer player : server.getPlayerList().getPlayers())
				if (player != discoverer && grantShared(player, instance) && definition.discoveryRequired()) {
					PacketDistributor.sendToPlayer(player, new WorldMapPoiDiscoveredMessage(definition.translationKey(), fallbackName(definition.id()),
						instance.customName(), instance.nameKey()));
					WorldMapPoiKnowledge.Entry entry = known(player.getUUID(), instance.markerId());
					if (entry != null)
						pushMarker(player, instance, definition, entry, generation);
				}
		}

		/** Returns true when the grant newly made the POI discovered for this player. */
		private boolean grantShared(ServerPlayer player, WorldMapPoiInstance instance) {
			WorldMapPoiKnowledge.Entry before = known(player.getUUID(), instance.markerId());
			return knowledge.grantShared(player.getUUID(), instance.markerId())
				&& (before == null || before.state() != WorldMapPoiKnowledge.State.DISCOVERED);
		}

		/** Grants every shared discovery to a player, silently. Callers send a cache reset afterwards. */
		private void syncShared(ServerPlayer player, WorldMapPoiDefinitions.Snapshot definitions) {
			for (UUID markerId : knowledge.sharedDiscoveries()) {
				WorldMapPoiInstance instance = instances.get(markerId);
				if (instance != null && instance.active() && definitions.accepts(instance) && sharesDiscovery(instance))
					grantShared(player, instance);
			}
		}

		/** Applies a change of the shared discovery setting and makes every client re-request its markers. */
		private void checkSharingSetting(WorldMapPoiDefinitions.Snapshot definitions) {
			boolean current = WorldMapServerConfig.sharedSignDiscovery();
			if (current == sharing)
				return;
			sharing = current;
			WitchercraftMod.LOGGER.info("Shared signpost discovery is now {}", current ? "on" : "off");
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (current)
					syncShared(player, definitions);
				sendReset(player, definitions);
			}
		}

		/** Deletes lifecycle-managed records anchored in this watched chunk whose world object is gone. */
		private void reconcileLoadedChunk(ServerLevel level, LevelChunk chunk) {
			Set<UUID> anchored = lifecycleByChunk.get(new ChunkKey(level.dimension().identifier(), chunk.getPos().x(), chunk.getPos().z()));
			if (anchored == null)
				return;
			WorldMapPoiProvider.ObservationContext context = new WorldMapPoiProvider.ObservationContext(level, chunk);
			for (UUID markerId : List.copyOf(anchored)) {
				WorldMapPoiInstance instance = instances.get(markerId);
				if (instance == null)
					continue;
				WorldMapPoiProvider<?, ?> provider = WorldMapPoiProviders.provider(instance.providerType()).orElse(null);
				if (provider != null && !provider.retainsLoadedInstance(context, instance) && remove(markerId))
					staleRemovals++;
			}
		}

		private void indexLifecycle(WorldMapPoiInstance instance) {
			lifecycleAnchors.put(new AnchorKey(instance.dimension(), instance.anchor().asLong()), instance.markerId());
			lifecycleByChunk.computeIfAbsent(ChunkKey.of(instance), ignored -> new java.util.LinkedHashSet<>()).add(instance.markerId());
		}

		private void unindexLifecycle(WorldMapPoiInstance instance) {
			lifecycleAnchors.remove(new AnchorKey(instance.dimension(), instance.anchor().asLong()), instance.markerId());
			ChunkKey chunk = ChunkKey.of(instance);
			Set<UUID> anchored = lifecycleByChunk.get(chunk);
			if (anchored != null) {
				anchored.remove(instance.markerId());
				if (anchored.isEmpty())
					lifecycleByChunk.remove(chunk);
			}
		}

		private static boolean lifecycleManaged(WorldMapPoiInstance instance) {
			return WorldMapPoiProviders.provider(instance.providerType()).map(WorldMapPoiProvider::lifecycleManaged).orElse(false);
		}

		private void accept(WorldMapPoiInstance observed, WorldMapPoiDefinitions.Snapshot definitions) {
			if (!definitions.accepts(observed))
				return;
			matchedObservations++;
			WorldMapPoiInstances.ObservationResult result = instances.observe(observed);
			switch (result) {
				case CREATED -> {
					uniqueInstances++;
					spatialIndex.upsert(observed);
					WitchercraftMod.LOGGER.info("Observed world-map POI: marker={}, definition={}, source={}, dimension={}, anchor=[{}, {}, {}], identity={}",
						observed.markerId(), observed.definitionId(), observed.sourceId(), observed.dimension(), observed.anchor().getX(),
						observed.anchor().getY(), observed.anchor().getZ(), observed.providerIdentity());
				}
				case UPDATED -> {
					refreshedInstances++;
					spatialIndex.upsert(observed);
				}
				case UNCHANGED -> duplicateObservations++;
				case COLLISION -> {
					observationCollisions++;
					WitchercraftMod.LOGGER.error("Stable POI marker collision for {} and identity '{}'", observed.markerId(), observed.providerIdentity());
				}
				case LIMIT_REACHED -> WitchercraftMod.LOGGER.error("Cannot retain POI {}: shared instance limit {} reached", observed.markerId(), WorldMapPoiInstances.MAX_INSTANCES);
			}
		}

		private void tick(WorldMapPoiDefinitions.Snapshot definitions) {
			long started = System.nanoTime();
			int tick = server.getTickCount();
			if (tick % DISCOVERY_INTERVAL_TICKS == 0)
				checkSharingSetting(definitions);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				int playerOffset = player.getUUID().hashCode();
				if (Math.floorMod(tick, DISCOVERY_INTERVAL_TICKS) == Math.floorMod(playerOffset, DISCOVERY_INTERVAL_TICKS))
					checkDiscovery(player, definitions);
				if (Math.floorMod(tick, REVEAL_INTERVAL_TICKS) == Math.floorMod(playerOffset, REVEAL_INTERVAL_TICKS))
					checkReveal(player, definitions);
			}
			long elapsed = System.nanoTime() - started;
			timedTicks++;
			totalTickNanos += elapsed;
			maximumTickNanos = Math.max(maximumTickNanos, elapsed);
			if (tick % DIAGNOSTIC_INTERVAL_TICKS == 0)
				logDiagnostics();
		}

		private void checkReveal(ServerPlayer player, WorldMapPoiDefinitions.Snapshot definitions) {
			if (maximumRevealRadius <= 0.0)
				return;
			Identifier dimension = player.level().dimension().identifier();
			List<UUID> candidates = spatialIndex.query(dimension, player.getX(), player.getZ(), maximumRevealRadius);
			revealCandidates += candidates.size();
			for (UUID markerId : candidates) {
				WorldMapPoiInstance instance = instances.get(markerId);
				if (instance == null || !instance.active() || known(player.getUUID(), markerId) != null)
					continue;
				WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
				if (definition == null || definition.revealRadius() <= 0.0 || !inside(player, instance.anchor(), definition.revealRadius()))
					continue;
				if (!knowledge.reveal(player.getUUID(), markerId, 0.0, 0.0))
					continue;
				reveals++;
				WitchercraftMod.LOGGER.info("Player {} revealed world-map POI {}", player.getGameProfile().name(), markerId);
				WorldMapPoiKnowledge.Entry revealed = known(player.getUUID(), markerId);
				if (revealed != null)
					pushMarker(player, instance, definition, revealed, definitions.generation());
				if (definition.discoveryRequired() && inside(player, instance.anchor(), definition.discoveryRadius()))
					discover(player, instance, definition, definitions.generation());
			}
		}

		private void checkDiscovery(ServerPlayer player, WorldMapPoiDefinitions.Snapshot definitions) {
			if (maximumDiscoveryRadius <= 0.0)
				return;
			Identifier dimension = player.level().dimension().identifier();
			List<UUID> candidates = spatialIndex.query(dimension, player.getX(), player.getZ(), maximumDiscoveryRadius);
			discoveryCandidates += candidates.size();
			for (UUID markerId : candidates) {
				WorldMapPoiInstance instance = instances.get(markerId);
				if (instance == null || !instance.active())
					continue;
				WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
				if (definition == null || !definition.discoveryRequired() || !inside(player, instance.anchor(), definition.discoveryRadius()))
					continue;
				WorldMapPoiKnowledge.Entry entry = known(player.getUUID(), markerId);
				if (entry != null && entry.state() == WorldMapPoiKnowledge.State.DISCOVERED) {
					// Visiting a POI known only through sharing makes it the player's own, silently, so it
					// stays discovered if sharing is switched off later.
					WorldMapPoiKnowledge.Entry own = knowledge.get(player.getUUID(), markerId);
					if (own != null && own.state() != WorldMapPoiKnowledge.State.DISCOVERED)
						knowledge.discover(player.getUUID(), markerId);
					continue;
				}
				if (entry == null && definition.revealRadius() > 0.0)
					continue;
				discover(player, instance, definition, definitions.generation());
			}
		}

		private void discover(ServerPlayer player, WorldMapPoiInstance instance, WorldMapPoiDefinition definition, long generation) {
			UUID markerId = instance.markerId();
			if (!knowledge.discover(player.getUUID(), markerId))
				return;
			discoveries++;
			PacketDistributor.sendToPlayer(player, new WorldMapPoiDiscoveredMessage(definition.translationKey(), fallbackName(definition.id()),
				instance.customName(), instance.nameKey()));
			player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP),
				SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.7F, 1.0F, player.getRandom().nextLong()));
			WorldMapPoiKnowledge.Entry discovered = known(player.getUUID(), markerId);
			if (discovered != null)
				pushMarker(player, instance, definition, discovered, generation);
			WitchercraftMod.LOGGER.info("Player {} discovered world-map POI {} ({})", player.getGameProfile().name(), markerId, definition.id());
			shareDiscovery(player, instance, definition, generation);
		}

		private void requestMarkers(ServerPlayer player, WorldMapPoiViewRequestMessage request, WorldMapPoiDefinitions.Snapshot definitions) {
			viewRequests++;
			Identifier currentDimension = player.level().dimension().identifier();
			if (!request.dimension().equals(currentDimension) || request.definitionGeneration() != definitions.generation()
				|| !allowRequest(player.getUUID(), request.cells().length)) {
				rejectedViewRequests++;
				complete(player, request.requestId(), false, definitions.generation(), new long[0]);
				return;
			}
			Set<Long> requestedCells = new HashSet<>();
			for (long cell : request.cells()) {
				if (!WorldMapPoiSpatialIndex.validCell(cell) || !requestedCells.add(cell)) {
					rejectedViewRequests++;
					complete(player, request.requestId(), false, definitions.generation(), new long[0]);
					return;
				}
			}

			List<WorldMapPoiMarker> markers = new ArrayList<>();
			for (WorldMapPoiKnowledge.Entry stored : knowledge.entries(player.getUUID())) {
				WorldMapPoiKnowledge.Entry entry = WorldMapPoiKnowledge.effective(stored, sharing);
				if (entry == null)
					continue;
				WorldMapPoiInstance instance = instances.get(entry.markerId());
				if (instance == null || !instance.active() || !instance.dimension().equals(currentDimension) || !definitions.accepts(instance))
					continue;
				WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
				if (definition == null)
					continue;
				WorldMapPoiMarker marker = marker(instance, definition, entry);
				long cell = WorldMapPoiSpatialIndex.packCell(WorldMapPoiSpatialIndex.cellFor(marker.x()), WorldMapPoiSpatialIndex.cellFor(marker.z()));
				if (!requestedCells.contains(cell))
					continue;
				if (markers.size() >= MAX_MARKERS_PER_REQUEST) {
					rejectedViewRequests++;
					complete(player, request.requestId(), false, definitions.generation(), new long[0]);
					return;
				}
				markers.add(marker);
			}
			markers.sort(Comparator.comparing(marker -> marker.markerId().toString()));
			for (int start = 0; start < markers.size(); start += WorldMapPoiDataMessage.MAX_MARKERS) {
				int end = Math.min(markers.size(), start + WorldMapPoiDataMessage.MAX_MARKERS);
				PacketDistributor.sendToPlayer(player, new WorldMapPoiDataMessage(request.requestId(), definitions.generation(), currentDimension, markers.subList(start, end)));
			}
			complete(player, request.requestId(), true, definitions.generation(), request.cells());
		}

		private void pushMarker(ServerPlayer player, WorldMapPoiInstance instance, WorldMapPoiDefinition definition,
			WorldMapPoiKnowledge.Entry entry, long generation) {
			PacketDistributor.sendToPlayer(player, new WorldMapPoiDataMessage(0, generation, instance.dimension(),
				List.of(marker(instance, definition, entry))));
		}

		private static WorldMapPoiMarker marker(WorldMapPoiInstance instance, WorldMapPoiDefinition definition, WorldMapPoiKnowledge.Entry entry) {
			double exactX = instance.anchor().getX() + 0.5;
			double exactZ = instance.anchor().getZ() + 0.5;
			if (entry.state() == WorldMapPoiKnowledge.State.REVEALED && definition.discoveryRequired())
				return new WorldMapPoiMarker.Unknown(entry.presentationId(), exactX, exactZ,
					definition.minimumZoom(), definition.defaultVisible());
			return new WorldMapPoiMarker.Discovered(entry.presentationId(), exactX, exactZ, definition.translationKey(), definition.descriptionTranslationKey(), definition.category(),
				definition.icon(), definition.minimumZoom(), definition.defaultVisible(), instance.customName(), instance.nameKey());
		}

		private void complete(ServerPlayer player, int requestId, boolean accepted, long generation, long[] cells) {
			PacketDistributor.sendToPlayer(player, new WorldMapPoiRequestCompleteMessage(requestId, accepted,
				WorldMapWorldIdentity.get(server), generation, cells));
		}

		private void sendReset(ServerPlayer player, WorldMapPoiDefinitions.Snapshot definitions) {
			PacketDistributor.sendToPlayer(player, new WorldMapPoiCacheResetMessage(WorldMapWorldIdentity.get(server), definitions.generation()));
		}

		private boolean allowRequest(UUID playerId, int cells) {
			int tick = server.getTickCount();
			return requestAllowances.computeIfAbsent(playerId, ignored -> new RequestAllowance(tick)).consume(tick, cells);
		}

		private void reconcile(WorldMapPoiDefinitions.Snapshot definitions) {
			maximumRevealRadius = 0.0;
			maximumDiscoveryRadius = 0.0;
			for (WorldMapPoiDefinition definition : definitions.definitions().values()) {
				maximumRevealRadius = Math.max(maximumRevealRadius, definition.revealRadius());
				if (definition.discoveryRequired())
					maximumDiscoveryRadius = Math.max(maximumDiscoveryRadius, definition.discoveryRadius());
			}
			for (WorldMapPoiInstance instance : instances.values()) {
				boolean accepted = definitions.accepts(instance);
				if (instance.active() && !accepted)
					instances.setActive(instance.markerId(), false);
				else if (!instance.active() && accepted && lifecycleManaged(instance))
					instances.setActive(instance.markerId(), true);
			}
			spatialIndex.rebuild(instances.values());
			for (UUID markerId : undiscoverable)
				spatialIndex.remove(markerId);
		}

		private void logDiagnostics() {
			long active = instances.values().stream().filter(WorldMapPoiInstance::active).count();
			double averageMicros = timedTicks == 0 ? 0.0 : totalTickNanos / (timedTicks * 1_000.0);
			double maximumMicros = maximumTickNanos / 1_000.0;
			WitchercraftMod.LOGGER.info("World-map POI state: watched_chunks={}, matched_observations={}, unique_instances={}, refreshed_instances={}, duplicate_observations={}, observation_collisions={}, retained_instances={}, active_instances={}, lifecycle_removals={}, stale_removals={}, reveals={}, discoveries={}, view_requests={}, rejected_view_requests={}, reveal_candidates={}, discovery_candidates={}, tick_avg_us={}, tick_max_us={}",
				watchedChunks, matchedObservations, uniqueInstances, refreshedInstances, duplicateObservations, observationCollisions,
				instances.values().size(), active, lifecycleRemovals, staleRemovals, reveals, discoveries, viewRequests, rejectedViewRequests, revealCandidates,
				discoveryCandidates, String.format(java.util.Locale.ROOT, "%.2f", averageMicros), String.format(java.util.Locale.ROOT, "%.2f", maximumMicros));
			timedTicks = 0;
			totalTickNanos = 0;
			maximumTickNanos = 0;
			revealCandidates = 0;
			discoveryCandidates = 0;
		}

		private static boolean inside(ServerPlayer player, BlockPos anchor, double radius) {
			double dx = player.getX() - (anchor.getX() + 0.5);
			double dz = player.getZ() - (anchor.getZ() + 0.5);
			return dx * dx + dz * dz <= radius * radius;
		}

		private static String fallbackName(Identifier definitionId) {
			String words = definitionId.getPath().replace('/', ' ').replace('_', ' ').replace('-', ' ');
			StringBuilder result = new StringBuilder(words.length());
			boolean capitalize = true;
			for (int index = 0; index < words.length(); index++) {
				char character = words.charAt(index);
				if (character == ' ') {
					capitalize = true;
					if (!result.isEmpty() && result.charAt(result.length() - 1) != ' ')
						result.append(' ');
				} else {
					result.append(capitalize ? Character.toUpperCase(character) : character);
					capitalize = false;
				}
			}
			return result.isEmpty() ? "Location" : result.toString();
		}

		private record AnchorKey(Identifier dimension, long position) {
		}

		private record ChunkKey(Identifier dimension, int x, int z) {
			private static ChunkKey of(WorldMapPoiInstance instance) {
				return new ChunkKey(instance.dimension(), instance.anchor().getX() >> 4, instance.anchor().getZ() >> 4);
			}
		}

		private static final class RequestAllowance {
			private static final int WINDOW_TICKS = 20;
			private static final int MAX_CELLS_PER_WINDOW = 128;
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
				if (count <= 0 || count > MAX_CELLS_PER_WINDOW - used)
					return false;
				used += count;
				return true;
			}
		}
	}
}

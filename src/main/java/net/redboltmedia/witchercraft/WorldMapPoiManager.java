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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

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
		WorldMapPoiDefinitions.Snapshot definitions = WorldMapPoiDefinitions.active();
		if (definitions.definitions().isEmpty())
			return;
		ServerState state = SERVERS.computeIfAbsent(event.getLevel().getServer(), ServerState::new);
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
			state.sendReset(player, WorldMapPoiDefinitions.active());
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

		private ServerState(MinecraftServer server) {
			this.server = server;
			instances = WorldMapPoiInstances.get(server);
			knowledge = WorldMapPoiKnowledge.get(server);
			reconcile(WorldMapPoiDefinitions.active());
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
				if (instance == null || !instance.active() || knowledge.get(player.getUUID(), markerId) != null)
					continue;
				WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
				if (definition == null || definition.revealRadius() <= 0.0 || !inside(player, instance.anchor(), definition.revealRadius()))
					continue;
				if (!knowledge.reveal(player.getUUID(), markerId, 0.0, 0.0))
					continue;
				reveals++;
				WitchercraftMod.LOGGER.info("Player {} revealed world-map POI {}", player.getGameProfile().name(), markerId);
				WorldMapPoiKnowledge.Entry revealed = knowledge.get(player.getUUID(), markerId);
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
				WorldMapPoiKnowledge.Entry entry = knowledge.get(player.getUUID(), markerId);
				if (entry != null && entry.state() == WorldMapPoiKnowledge.State.DISCOVERED)
					continue;
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
			PacketDistributor.sendToPlayer(player, new WorldMapPoiDiscoveredMessage(definition.translationKey(), fallbackName(definition.id())));
			player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP),
				SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.7F, 1.0F, player.getRandom().nextLong()));
			WorldMapPoiKnowledge.Entry discovered = knowledge.get(player.getUUID(), markerId);
			if (discovered != null)
				pushMarker(player, instance, definition, discovered, generation);
			WitchercraftMod.LOGGER.info("Player {} discovered world-map POI {} ({})", player.getGameProfile().name(), markerId, definition.id());
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
			for (WorldMapPoiKnowledge.Entry entry : knowledge.entries(player.getUUID())) {
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
				definition.icon(), definition.minimumZoom(), definition.defaultVisible());
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
			for (WorldMapPoiInstance instance : instances.values())
				if (instance.active() && !definitions.accepts(instance))
					instances.setActive(instance.markerId(), false);
			spatialIndex.rebuild(instances.values());
		}

		private void logDiagnostics() {
			long active = instances.values().stream().filter(WorldMapPoiInstance::active).count();
			double averageMicros = timedTicks == 0 ? 0.0 : totalTickNanos / (timedTicks * 1_000.0);
			double maximumMicros = maximumTickNanos / 1_000.0;
			WitchercraftMod.LOGGER.info("World-map POI state: watched_chunks={}, matched_observations={}, unique_instances={}, refreshed_instances={}, duplicate_observations={}, observation_collisions={}, retained_instances={}, active_instances={}, reveals={}, discoveries={}, view_requests={}, rejected_view_requests={}, reveal_candidates={}, discovery_candidates={}, tick_avg_us={}, tick_max_us={}",
				watchedChunks, matchedObservations, uniqueInstances, refreshedInstances, duplicateObservations, observationCollisions,
				instances.values().size(), active, reveals, discoveries, viewRequests, rejectedViewRequests, revealCandidates,
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

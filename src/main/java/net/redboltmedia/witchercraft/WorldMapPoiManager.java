package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Persistent POI observation, spatial indexing, reveal, and discovery owner. */
@EventBusSubscriber
public final class WorldMapPoiManager {
	private static final int REVEAL_INTERVAL_TICKS = 200;
	private static final int DISCOVERY_INTERVAL_TICKS = 20;
	private static final int DIAGNOSTIC_INTERVAL_TICKS = 1200;
	private static final Map<MinecraftServer, ServerState> SERVERS = new ConcurrentHashMap<>();

	private WorldMapPoiManager() {
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event) {
		if (!event.getLevel().dimension().equals(Level.OVERWORLD))
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
		SERVERS.computeIfAbsent(event.getServer(), ServerState::new).tick(WorldMapPoiDefinitions.active());
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		SERVERS.remove(event.getServer());
	}

	public static void onDefinitionsReloaded(WorldMapPoiDefinitions.Snapshot definitions) {
		for (ServerState state : SERVERS.values())
			state.reconcile(definitions);
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
		private double maximumRevealRadius;
		private double maximumDiscoveryRadius;
		private long watchedChunks;
		private long matchedObservations;
		private long uniqueInstances;
		private long refreshedInstances;
		private long reveals;
		private long discoveries;

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
				case UNCHANGED -> refreshedInstances++;
				case COLLISION -> WitchercraftMod.LOGGER.error("Stable POI marker collision for {} and identity '{}'", observed.markerId(), observed.providerIdentity());
				case LIMIT_REACHED -> WitchercraftMod.LOGGER.error("Cannot retain POI {}: shared instance limit {} reached", observed.markerId(), WorldMapPoiInstances.MAX_INSTANCES);
			}
		}

		private void tick(WorldMapPoiDefinitions.Snapshot definitions) {
			int tick = server.getTickCount();
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				int playerOffset = player.getUUID().hashCode();
				if (Math.floorMod(tick, DISCOVERY_INTERVAL_TICKS) == Math.floorMod(playerOffset, DISCOVERY_INTERVAL_TICKS))
					checkDiscovery(player, definitions);
				if (Math.floorMod(tick, REVEAL_INTERVAL_TICKS) == Math.floorMod(playerOffset, REVEAL_INTERVAL_TICKS))
					checkReveal(player, definitions);
			}
			if (tick % DIAGNOSTIC_INTERVAL_TICKS == 0)
				logDiagnostics();
		}

		private void checkReveal(ServerPlayer player, WorldMapPoiDefinitions.Snapshot definitions) {
			if (maximumRevealRadius <= 0.0)
				return;
			Identifier dimension = player.level().dimension().identifier();
			for (UUID markerId : spatialIndex.query(dimension, player.getX(), player.getZ(), maximumRevealRadius)) {
				WorldMapPoiInstance instance = instances.get(markerId);
				if (instance == null || !instance.active() || knowledge.get(player.getUUID(), markerId) != null)
					continue;
				WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
				if (definition == null || definition.revealRadius() <= 0.0 || !inside(player, instance.anchor(), definition.revealRadius()))
					continue;
				double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
				double radius = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * definition.uncertaintyRadius();
				if (!knowledge.reveal(player.getUUID(), markerId, Math.cos(angle) * radius, Math.sin(angle) * radius))
					continue;
				reveals++;
				WitchercraftMod.LOGGER.info("Player {} revealed world-map POI {}", player.getGameProfile().name(), markerId);
				if (inside(player, instance.anchor(), definition.discoveryRadius()))
					discover(player, markerId, definition);
			}
		}

		private void checkDiscovery(ServerPlayer player, WorldMapPoiDefinitions.Snapshot definitions) {
			if (maximumDiscoveryRadius <= 0.0)
				return;
			Identifier dimension = player.level().dimension().identifier();
			for (UUID markerId : spatialIndex.query(dimension, player.getX(), player.getZ(), maximumDiscoveryRadius)) {
				WorldMapPoiInstance instance = instances.get(markerId);
				if (instance == null || !instance.active())
					continue;
				WorldMapPoiDefinition definition = definitions.definitions().get(instance.definitionId());
				if (definition == null || !inside(player, instance.anchor(), definition.discoveryRadius()))
					continue;
				WorldMapPoiKnowledge.Entry entry = knowledge.get(player.getUUID(), markerId);
				if (entry != null && entry.state() == WorldMapPoiKnowledge.State.DISCOVERED)
					continue;
				if (entry == null && definition.revealRadius() > 0.0)
					continue;
				discover(player, markerId, definition);
			}
		}

		private void discover(ServerPlayer player, UUID markerId, WorldMapPoiDefinition definition) {
			if (!knowledge.discover(player.getUUID(), markerId))
				return;
			discoveries++;
			Component name = Component.translatableWithFallback(definition.translationKey(), fallbackName(definition.id()));
			player.sendSystemMessage(Component.translatableWithFallback("message.witchercraft.poi.discovered", "Discovered: %s", name), true);
			player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP),
				SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.7F, 1.0F, player.getRandom().nextLong()));
			WitchercraftMod.LOGGER.info("Player {} discovered world-map POI {} ({})", player.getGameProfile().name(), markerId, definition.id());
		}

		private void reconcile(WorldMapPoiDefinitions.Snapshot definitions) {
			maximumRevealRadius = 0.0;
			maximumDiscoveryRadius = 0.0;
			for (WorldMapPoiDefinition definition : definitions.definitions().values()) {
				maximumRevealRadius = Math.max(maximumRevealRadius, definition.revealRadius());
				maximumDiscoveryRadius = Math.max(maximumDiscoveryRadius, definition.discoveryRadius());
			}
			for (WorldMapPoiInstance instance : instances.values())
				if (instance.active() && !definitions.accepts(instance))
					instances.setActive(instance.markerId(), false);
			spatialIndex.rebuild(instances.values());
		}

		private void logDiagnostics() {
			long active = instances.values().stream().filter(WorldMapPoiInstance::active).count();
			WitchercraftMod.LOGGER.info("World-map POI state: watched_chunks={}, matched_observations={}, unique_instances={}, refreshed_instances={}, retained_instances={}, active_instances={}, reveals={}, discoveries={}",
				watchedChunks, matchedObservations, uniqueInstances, refreshedInstances, instances.values().size(), active, reveals, discoveries);
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
	}
}

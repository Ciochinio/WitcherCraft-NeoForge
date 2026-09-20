package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Loaded-chunk observation and in-memory POI registry for Batch 4B. */
@EventBusSubscriber
public final class WorldMapPoiManager {
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
		ServerState state = SERVERS.computeIfAbsent(event.getLevel().getServer(), ignored -> new ServerState());
		state.watchedChunks++;
		definitions.providers().observeLoadedChunk(new WorldMapPoiProvider.ObservationContext(event.getLevel(), event.getChunk()),
			instance -> state.accept(instance, definitions));
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		ServerState state = SERVERS.get(event.getServer());
		if (state != null && event.getServer().getTickCount() % DIAGNOSTIC_INTERVAL_TICKS == 0)
			state.logDiagnostics();
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
		return state == null ? java.util.List.of() : java.util.List.copyOf(state.instances.values());
	}

	private static final class ServerState {
		private final Map<UUID, WorldMapPoiInstance> instances = new LinkedHashMap<>();
		private long watchedChunks;
		private long matchedObservations;
		private long uniqueInstances;
		private long refreshedInstances;

		private void accept(WorldMapPoiInstance observed, WorldMapPoiDefinitions.Snapshot definitions) {
			if (!definitions.accepts(observed))
				return;
			matchedObservations++;
			WorldMapPoiInstance previous = instances.get(observed.markerId());
			if (previous == null) {
				instances.put(observed.markerId(), observed);
				uniqueInstances++;
				WitchercraftMod.LOGGER.info("Observed world-map POI: marker={}, definition={}, source={}, dimension={}, anchor=[{}, {}, {}], identity={}",
					observed.markerId(), observed.definitionId(), observed.sourceId(), observed.dimension(), observed.anchor().getX(), observed.anchor().getY(), observed.anchor().getZ(), observed.providerIdentity());
			} else {
				if (!previous.providerIdentity().equals(observed.providerIdentity())) {
					WitchercraftMod.LOGGER.error("Stable POI marker collision for {} between '{}' and '{}'", observed.markerId(), previous.providerIdentity(), observed.providerIdentity());
					return;
				}
				instances.put(observed.markerId(), observed);
				refreshedInstances++;
			}
		}

		private void reconcile(WorldMapPoiDefinitions.Snapshot definitions) {
			// Reload may suppress a retained record, but only a fresh provider observation
			// may reactivate it after its definition returns.
			instances.replaceAll((id, instance) -> instance.withActive(instance.active() && definitions.accepts(instance)));
		}

		private void logDiagnostics() {
			long active = instances.values().stream().filter(WorldMapPoiInstance::active).count();
			WitchercraftMod.LOGGER.info("World-map POI observation: watched_chunks={}, matched_observations={}, unique_instances={}, refreshed_instances={}, retained_instances={}, active_instances={}",
				watchedChunks, matchedObservations, uniqueInstances, refreshedInstances, instances.size(), active);
		}
	}
}

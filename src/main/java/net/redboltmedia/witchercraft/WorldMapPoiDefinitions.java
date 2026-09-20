package net.redboltmedia.witchercraft;

import com.mojang.serialization.DataResult;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Datapack reload owner for JSON files in each namespace's {@code witchercraft_pois} directory. */
@EventBusSubscriber
public final class WorldMapPoiDefinitions {
	private static final Identifier LISTENER_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_poi_definitions");
	private static final FileToIdConverter FILES = FileToIdConverter.json("witchercraft_pois");
	private static volatile Snapshot active = Snapshot.empty();

	private WorldMapPoiDefinitions() {
	}

	@SubscribeEvent
	public static void addReloadListener(AddServerReloadListenersEvent event) {
		event.addListener(LISTENER_ID, new ReloadListener(event.getServerResources().getRegistryLookup()));
	}

	public static Snapshot active() {
		return active;
	}

	private static final class ReloadListener extends SimpleJsonResourceReloadListener<WorldMapPoiDefinition.Template> {
		private final HolderLookup.Provider registries;

		private ReloadListener(HolderLookup.Provider registries) {
			super(WorldMapPoiDefinition.CODEC, FILES);
			this.registries = registries;
		}

		@Override
		protected void apply(Map<Identifier, WorldMapPoiDefinition.Template> decoded, ResourceManager manager, ProfilerFiller profiler) {
			List<Map.Entry<Identifier, WorldMapPoiDefinition.Template>> ordered = new ArrayList<>(decoded.entrySet());
			ordered.sort(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)));
			Map<Identifier, WorldMapPoiDefinition> accepted = new LinkedHashMap<>();
			Set<String> providerKeys = new HashSet<>();
			int rejected = 0;
			for (Map.Entry<Identifier, WorldMapPoiDefinition.Template> entry : ordered) {
				if (accepted.size() >= WorldMapPoiDefinition.MAX_DEFINITIONS) {
					WitchercraftMod.LOGGER.error("Rejected POI definition {}: definition limit {} reached", entry.getKey(), WorldMapPoiDefinition.MAX_DEFINITIONS);
					rejected++;
					continue;
				}
				DataResult<WorldMapPoiDefinition> result = WorldMapPoiDefinition.resolve(entry.getKey(), entry.getValue(), registries);
				Optional<WorldMapPoiDefinition> resolved = result.resultOrPartial(error -> WitchercraftMod.LOGGER.error("Rejected POI definition {}: {}", entry.getKey(), error));
				if (resolved.isEmpty()) {
					rejected++;
					continue;
				}
				WorldMapPoiDefinition definition = resolved.get();
				Optional<String> uniqueKey = definition.provider().uniquenessKey();
				if (uniqueKey.isPresent() && !providerKeys.add(uniqueKey.get())) {
					WitchercraftMod.LOGGER.error("Rejected POI definition {}: provider source is already claimed ({})", entry.getKey(), uniqueKey.get());
					rejected++;
					continue;
				}
				accepted.put(entry.getKey(), definition);
			}

			DataResult<WorldMapPoiProviders.PreparedProviders> preparedResult = WorldMapPoiProviders.prepare(accepted.values(), registries);
			Optional<WorldMapPoiProviders.PreparedProviders> prepared = preparedResult.resultOrPartial(error -> WitchercraftMod.LOGGER.error("Could not publish POI definitions: {}", error));
			if (prepared.isEmpty()) {
				WitchercraftMod.LOGGER.error("Keeping the previous POI definition snapshot because provider preparation failed");
				return;
			}
			long nextGeneration = active.generation() == Long.MAX_VALUE ? 1L : active.generation() + 1L;
			Snapshot replacement = new Snapshot(Map.copyOf(accepted), prepared.get(), nextGeneration);
			active = replacement;
			WorldMapPoiManager.onDefinitionsReloaded(replacement);
			WitchercraftMod.LOGGER.info("World-map POI definitions loaded: valid={}, rejected={}", accepted.size(), rejected);
		}
	}

	public record Snapshot(Map<Identifier, WorldMapPoiDefinition> definitions, WorldMapPoiProviders.PreparedProviders providers, long generation) {
		public Snapshot {
			definitions = Map.copyOf(definitions);
			if (generation < 0)
				throw new IllegalArgumentException("POI definition generation may not be negative");
		}

		private static Snapshot empty() {
			return new Snapshot(Map.of(), new WorldMapPoiProviders.PreparedProviders(Map.of()), 0L);
		}

		public boolean accepts(WorldMapPoiInstance instance) {
			WorldMapPoiDefinition definition = definitions.get(instance.definitionId());
			return definition != null && definition.provider().type().equals(instance.providerType()) && definition.provider().matchesSource(instance.sourceId());
		}
	}
}

package net.redboltmedia.witchercraft;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Internal registry for reusable POI source mechanisms. */
public final class WorldMapPoiProviders {
	private static final Map<Identifier, WorldMapPoiProvider<?, ?>> PROVIDERS = new LinkedHashMap<>();

	static {
		register(WorldMapStructurePoiProvider.INSTANCE);
	}

	private WorldMapPoiProviders() {
	}

	private static void register(WorldMapPoiProvider<?, ?> provider) {
		if (PROVIDERS.putIfAbsent(provider.id(), provider) != null)
			throw new IllegalStateException("Duplicate world-map POI provider: " + provider.id());
	}

	public static DataResult<Binding<?>> decodeAndValidate(Dynamic<?> encoded, HolderLookup.Provider registries) {
		Optional<String> rawType = encoded.get("type").asString().result();
		if (rawType.isEmpty())
			return DataResult.error(() -> "POI provider requires a string type");
		Identifier type = Identifier.tryParse(rawType.get());
		if (type == null)
			return DataResult.error(() -> "Invalid POI provider type: " + rawType.get());
		WorldMapPoiProvider<?, ?> provider = PROVIDERS.get(type);
		if (provider == null)
			return DataResult.error(() -> "Unknown POI provider type: " + type);
		return decodeTyped(provider, encoded, registries);
	}

	private static <C, P> DataResult<Binding<?>> decodeTyped(WorldMapPoiProvider<C, P> provider, Dynamic<?> encoded, HolderLookup.Provider registries) {
		return provider.codec().parse(encoded).flatMap(configuration -> provider.validate(configuration, registries)
			.map(validated -> new Binding<>(provider, validated)));
	}

	public static DataResult<PreparedProviders> prepare(Collection<WorldMapPoiDefinition> definitions, HolderLookup.Provider registries) {
		Map<Identifier, PreparedProvider> prepared = new LinkedHashMap<>();
		for (WorldMapPoiProvider<?, ?> provider : PROVIDERS.values()) {
			List<WorldMapPoiDefinition> matching = definitions.stream().filter(definition -> definition.provider().provider() == provider).toList();
			if (!matching.isEmpty()) {
				DataResult<PreparedProvider> result = prepareTyped(provider, matching, registries);
				Optional<PreparedProvider> value = result.result();
				if (value.isEmpty())
					return DataResult.error(() -> result.error().map(error -> error.message()).orElse("Could not prepare POI provider " + provider.id()));
				prepared.put(provider.id(), value.get());
			}
		}
		return DataResult.success(new PreparedProviders(Map.copyOf(prepared)));
	}

	@SuppressWarnings("unchecked")
	private static <C, P> DataResult<PreparedProvider> prepareTyped(WorldMapPoiProvider<C, P> provider, List<WorldMapPoiDefinition> definitions, HolderLookup.Provider registries) {
		List<WorldMapPoiProvider.ConfiguredDefinition<C>> configured = new ArrayList<>(definitions.size());
		for (WorldMapPoiDefinition definition : definitions)
			configured.add(new WorldMapPoiProvider.ConfiguredDefinition<>(definition, (C) definition.provider().configuration()));
		return provider.prepare(List.copyOf(configured), registries).map(value -> new PreparedProvider(provider, value));
	}

	public static final class Binding<C> {
		private final WorldMapPoiProvider<C, ?> provider;
		private final C configuration;

		private Binding(WorldMapPoiProvider<C, ?> provider, C configuration) {
			this.provider = provider;
			this.configuration = configuration;
		}

		public WorldMapPoiProvider<C, ?> provider() {
			return provider;
		}

		public C configuration() {
			return configuration;
		}

		public Identifier type() {
			return provider.id();
		}

		public Optional<String> uniquenessKey() {
			return provider.uniquenessKey(configuration).map(key -> provider.id() + "|" + key);
		}

		public boolean matchesSource(Identifier sourceId) {
			return provider.matchesSource(configuration, sourceId);
		}
	}

	public record PreparedProviders(Map<Identifier, PreparedProvider> providers) {
		public PreparedProviders {
			providers = Map.copyOf(providers);
		}

		public void observeLoadedChunk(WorldMapPoiProvider.ObservationContext context, WorldMapPoiProvider.InstanceSink sink) {
			for (PreparedProvider provider : providers.values())
				provider.observeLoadedChunk(context, sink);
		}
	}

	private static final class PreparedProvider {
		private final WorldMapPoiProvider<Object, Object> provider;
		private final Object prepared;

		@SuppressWarnings("unchecked")
		private <C, P> PreparedProvider(WorldMapPoiProvider<C, P> provider, P prepared) {
			this.provider = (WorldMapPoiProvider<Object, Object>) provider;
			this.prepared = prepared;
		}

		private void observeLoadedChunk(WorldMapPoiProvider.ObservationContext context, WorldMapPoiProvider.InstanceSink sink) {
			provider.observeLoadedChunk(context, prepared, sink);
		}
	}
}

package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** POI provider for vanilla and modded point-of-interest records in already-loaded chunks. */
public final class WorldMapPoiTypeProvider implements WorldMapPoiProvider<WorldMapPoiTypeProvider.Configuration, WorldMapPoiTypeProvider.Prepared> {
	public static final WorldMapPoiTypeProvider INSTANCE = new WorldMapPoiTypeProvider();
	public static final Identifier ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "poi_type");

	private static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("poi_type").forGetter(Configuration::poiType)
	).apply(instance, Configuration::new));

	private WorldMapPoiTypeProvider() {
	}

	@Override
	public Identifier id() {
		return ID;
	}

	@Override
	public Codec<Configuration> codec() {
		return CODEC;
	}

	@Override
	public DataResult<Configuration> validate(Configuration configuration, HolderLookup.Provider registries) {
		if (registries.lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE).get(poiTypeKey(configuration.poiType())).isEmpty())
			return DataResult.error(() -> "Unknown point-of-interest type: " + configuration.poiType());
		return DataResult.success(configuration);
	}

	@Override
	public DataResult<Prepared> prepare(List<ConfiguredDefinition<Configuration>> definitions, HolderLookup.Provider registries) {
		Map<ResourceKey<PoiType>, PreparedDefinition> byType = new LinkedHashMap<>();
		for (ConfiguredDefinition<Configuration> configured : definitions) {
			ResourceKey<PoiType> key = poiTypeKey(configured.configuration().poiType());
			PreparedDefinition previous = byType.putIfAbsent(key, new PreparedDefinition(configured.definition(), configured.configuration().poiType()));
			if (previous != null)
				return DataResult.error(() -> "Point-of-interest type " + configured.configuration().poiType() + " is claimed by both "
					+ previous.definition().id() + " and " + configured.definition().id());
		}
		return DataResult.success(new Prepared(Map.copyOf(byType)));
	}

	@Override
	public void observeLoadedChunk(ObservationContext context, Prepared prepared, InstanceSink sink) {
		Identifier dimension = context.level().dimension().identifier();
		context.level().getPoiManager().getInChunk(holder -> holder.unwrapKey().map(prepared.byType()::containsKey).orElse(false),
			context.chunk().getPos(), PoiManager.Occupancy.ANY).forEach(record -> record.getPoiType().unwrapKey().ifPresent(key -> {
				PreparedDefinition configured = prepared.byType().get(key);
				if (configured == null)
					return;
				var anchor = record.getPos();
				String identity = ID + "|" + configured.poiTypeId() + "|" + dimension + "|" + anchor.getX() + "," + anchor.getY() + "," + anchor.getZ();
				sink.accept(WorldMapPoiInstance.observed(configured.definition().id(), ID, configured.poiTypeId(), identity, dimension, anchor));
			}));
	}

	@Override
	public Optional<String> uniquenessKey(Configuration configuration) {
		return Optional.of(configuration.poiType().toString());
	}

	@Override
	public boolean matchesSource(Configuration configuration, Identifier sourceId) {
		return configuration.poiType().equals(sourceId);
	}

	private static ResourceKey<PoiType> poiTypeKey(Identifier id) {
		return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id);
	}

	public record Configuration(Identifier poiType) {
	}

	public record Prepared(Map<ResourceKey<PoiType>, PreparedDefinition> byType) {
		public Prepared {
			byType = Map.copyOf(byType);
		}
	}

	private record PreparedDefinition(WorldMapPoiDefinition definition, Identifier poiTypeId) {
	}
}

package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/Fast Travel). POI provider for fast-travel
 * signposts. Signs are lifecycle-managed: {@link FastTravelSigns} creates and deletes their records
 * when the block is placed or destroyed, so chunk observation adds nothing. When a player watches a
 * chunk containing a retained sign anchor, the loaded chunk is checked and a record whose sign no
 * longer stands (for example after /setblock) is deleted.
 */
public final class FastTravelSignPoiProvider implements WorldMapPoiProvider<FastTravelSignPoiProvider.Configuration, FastTravelSignPoiProvider.Prepared> {
	public static final FastTravelSignPoiProvider INSTANCE = new FastTravelSignPoiProvider();
	private static final Codec<Configuration> CODEC = MapCodec.unit(Configuration::new).codec();

	private FastTravelSignPoiProvider() {
	}

	@Override
	public Identifier id() {
		return FastTravelSigns.PROVIDER_ID;
	}

	@Override
	public Codec<Configuration> codec() {
		return CODEC;
	}

	@Override
	public DataResult<Configuration> validate(Configuration configuration, HolderLookup.Provider registries) {
		return DataResult.success(configuration);
	}

	@Override
	public DataResult<Prepared> prepare(List<ConfiguredDefinition<Configuration>> definitions, HolderLookup.Provider registries) {
		return DataResult.success(new Prepared());
	}

	@Override
	public void observeLoadedChunk(ObservationContext context, Prepared prepared, InstanceSink sink) {
		// Records are created by the sign block's placement procedure, never by observation.
	}

	@Override
	public Optional<String> uniquenessKey(Configuration configuration) {
		return Optional.of("signs");
	}

	@Override
	public boolean matchesSource(Configuration configuration, Identifier sourceId) {
		return FastTravelSigns.SOURCE_PLAYER.equals(sourceId) || FastTravelSigns.SOURCE_VILLAGE.equals(sourceId);
	}

	@Override
	public boolean lifecycleManaged() {
		return true;
	}

	@Override
	public boolean retainsLoadedInstance(ObservationContext context, WorldMapPoiInstance instance) {
		return FastTravelSigns.isCompleteSign(context.chunk(), instance.anchor());
	}

	@Override
	public boolean sharesDiscovery() {
		return true;
	}

	public record Configuration() {
	}

	public record Prepared() {
	}
}

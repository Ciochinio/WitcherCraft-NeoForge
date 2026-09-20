package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;
import java.util.Optional;

/**
 * Converts already-loaded world objects into stable map POI observations.
 * Providers must never locate structures, add tickets, or load chunks.
 */
public interface WorldMapPoiProvider<C, P> {
	Identifier id();

	Codec<C> codec();

	DataResult<C> validate(C configuration, HolderLookup.Provider registries);

	DataResult<P> prepare(List<ConfiguredDefinition<C>> definitions, HolderLookup.Provider registries);

	void observeLoadedChunk(ObservationContext context, P prepared, InstanceSink sink);

	/** A key used to reject ambiguous definitions handled by this provider. */
	Optional<String> uniquenessKey(C configuration);

	/** Whether a retained runtime instance still matches this configuration. */
	boolean matchesSource(C configuration, Identifier sourceId);

	record ConfiguredDefinition<C>(WorldMapPoiDefinition definition, C configuration) {
	}

	record ObservationContext(ServerLevel level, LevelChunk chunk) {
	}

	@FunctionalInterface
	interface InstanceSink {
		void accept(WorldMapPoiInstance instance);
	}
}

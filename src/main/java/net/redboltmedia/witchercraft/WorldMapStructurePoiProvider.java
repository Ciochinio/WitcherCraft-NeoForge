package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** POI provider for genuine Minecraft structure starts in already-loaded chunks. */
public final class WorldMapStructurePoiProvider implements WorldMapPoiProvider<WorldMapStructurePoiProvider.Configuration, WorldMapStructurePoiProvider.Prepared> {
	public static final WorldMapStructurePoiProvider INSTANCE = new WorldMapStructurePoiProvider();
	public static final Identifier ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "structure");
	private static final String BOUNDING_BOX_CENTER = "bounding_box_center";

	private static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("structure").forGetter(Configuration::structure),
		Codec.STRING.optionalFieldOf("anchor", BOUNDING_BOX_CENTER).forGetter(Configuration::anchor)
	).apply(instance, Configuration::new));

	private WorldMapStructurePoiProvider() {
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
		if (!BOUNDING_BOX_CENTER.equals(configuration.anchor()))
			return DataResult.error(() -> "Unknown structure anchor strategy: " + configuration.anchor());
		if (registries.lookupOrThrow(Registries.STRUCTURE).get(structureKey(configuration.structure())).isEmpty())
			return DataResult.error(() -> "Unknown structure: " + configuration.structure());
		return DataResult.success(configuration);
	}

	@Override
	public DataResult<Prepared> prepare(List<ConfiguredDefinition<Configuration>> definitions, HolderLookup.Provider registries) {
		HolderLookup.RegistryLookup<Structure> structures = registries.lookupOrThrow(Registries.STRUCTURE);
		Map<Structure, PreparedDefinition> byStructure = new LinkedHashMap<>();
		for (ConfiguredDefinition<Configuration> configured : definitions) {
			Structure structure = structures.get(structureKey(configured.configuration().structure())).map(holder -> holder.value()).orElse(null);
			if (structure == null)
				return DataResult.error(() -> "Structure disappeared while preparing POI definitions: " + configured.configuration().structure());
			byStructure.put(structure, new PreparedDefinition(configured.definition(), configured.configuration().structure()));
		}
		return DataResult.success(new Prepared(Map.copyOf(byStructure)));
	}

	@Override
	public void observeLoadedChunk(ObservationContext context, Prepared prepared, InstanceSink sink) {
		for (Map.Entry<Structure, PreparedDefinition> entry : prepared.byStructure().entrySet()) {
			StructureStart start = context.chunk().getStartForStructure(entry.getKey());
			if (start == null || !start.isValid())
				continue;
			PreparedDefinition configured = entry.getValue();
			BlockPos anchor = start.getBoundingBox().getCenter();
			Identifier dimension = context.level().dimension().identifier();
			String identity = ID + "|" + configured.structureId() + "|" + dimension + "|" + start.getChunkPos().x() + "," + start.getChunkPos().z();
			sink.accept(WorldMapPoiInstance.observed(configured.definition().id(), ID, configured.structureId(), identity, dimension, anchor));
		}
	}

	@Override
	public Optional<String> uniquenessKey(Configuration configuration) {
		return Optional.of(configuration.structure().toString());
	}

	@Override
	public boolean matchesSource(Configuration configuration, Identifier sourceId) {
		return configuration.structure().equals(sourceId);
	}

	private static ResourceKey<Structure> structureKey(Identifier id) {
		return ResourceKey.create(Registries.STRUCTURE, id);
	}

	public record Configuration(Identifier structure, String anchor) {
	}

	public record Prepared(Map<Structure, PreparedDefinition> byStructure) {
		public Prepared {
			byStructure = Map.copyOf(byStructure);
		}
	}

	private record PreparedDefinition(WorldMapPoiDefinition definition, Identifier structureId) {
	}
}

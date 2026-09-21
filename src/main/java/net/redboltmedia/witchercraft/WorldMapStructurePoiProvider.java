package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** POI provider for genuine Minecraft structure starts in already-loaded chunks. */
public final class WorldMapStructurePoiProvider implements WorldMapPoiProvider<WorldMapStructurePoiProvider.Configuration, WorldMapStructurePoiProvider.Prepared> {
	public static final WorldMapStructurePoiProvider INSTANCE = new WorldMapStructurePoiProvider();
	public static final Identifier ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "structure");
	private static final String BOUNDING_BOX_CENTER = "bounding_box_center";

	private static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.optionalFieldOf("structure").forGetter(Configuration::structure),
		Identifier.CODEC.optionalFieldOf("structure_tag").forGetter(Configuration::structureTag),
		Codec.STRING.optionalFieldOf("anchor", BOUNDING_BOX_CENTER).forGetter(Configuration::anchor)
	).apply(instance, (structure, structureTag, anchor) -> new Configuration(structure, structureTag, anchor, Set.of())));

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
		if (configuration.structure().isPresent() == configuration.structureTag().isPresent())
			return DataResult.error(() -> "Structure provider requires exactly one of 'structure' or 'structure_tag'");

		HolderLookup.RegistryLookup<Structure> structures = registries.lookupOrThrow(Registries.STRUCTURE);
		Set<Identifier> resolved = new HashSet<>();
		if (configuration.structure().isPresent()) {
			Identifier structureId = configuration.structure().get();
			if (structures.get(structureKey(structureId)).isEmpty())
				return DataResult.error(() -> "Unknown structure: " + structureId);
			resolved.add(structureId);
		} else {
			Identifier tagId = configuration.structureTag().orElseThrow();
			Optional<net.minecraft.core.HolderSet.Named<Structure>> tag = structures.get(structureTagKey(tagId));
			if (tag.isEmpty())
				return DataResult.error(() -> "Unknown structure tag: #" + tagId);
			tag.get().stream().map(Holder::unwrapKey).flatMap(Optional::stream).map(ResourceKey::identifier).forEach(resolved::add);
			if (resolved.isEmpty())
				return DataResult.error(() -> "Structure tag is empty: #" + tagId);
		}
		return DataResult.success(configuration.withResolvedStructures(Set.copyOf(resolved)));
	}

	@Override
	public DataResult<Prepared> prepare(List<ConfiguredDefinition<Configuration>> definitions, HolderLookup.Provider registries) {
		HolderLookup.RegistryLookup<Structure> structures = registries.lookupOrThrow(Registries.STRUCTURE);
		Map<Structure, PreparedDefinition> byStructure = new LinkedHashMap<>();
		for (ConfiguredDefinition<Configuration> configured : definitions) {
			for (Identifier structureId : configured.configuration().resolvedStructures()) {
				Structure structure = structures.get(structureKey(structureId)).map(Holder::value).orElse(null);
				if (structure == null)
					return DataResult.error(() -> "Structure disappeared while preparing POI definitions: " + structureId);
				PreparedDefinition previous = byStructure.putIfAbsent(structure, new PreparedDefinition(configured.definition(), structureId));
				if (previous != null)
					return DataResult.error(() -> "Structure " + structureId + " is claimed by both " + previous.definition().id()
						+ " and " + configured.definition().id());
			}
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
		return configuration.structure().map(id -> "structure|" + id)
			.or(() -> configuration.structureTag().map(id -> "structure_tag|" + id));
	}

	@Override
	public boolean matchesSource(Configuration configuration, Identifier sourceId) {
		return configuration.resolvedStructures().contains(sourceId);
	}

	private static ResourceKey<Structure> structureKey(Identifier id) {
		return ResourceKey.create(Registries.STRUCTURE, id);
	}

	private static TagKey<Structure> structureTagKey(Identifier id) {
		return TagKey.create(Registries.STRUCTURE, id);
	}

	public record Configuration(Optional<Identifier> structure, Optional<Identifier> structureTag, String anchor, Set<Identifier> resolvedStructures) {
		public Configuration {
			resolvedStructures = Set.copyOf(resolvedStructures);
		}

		private Configuration withResolvedStructures(Set<Identifier> structures) {
			return new Configuration(structure, structureTag, anchor, structures);
		}
	}

	public record Prepared(Map<Structure, PreparedDefinition> byStructure) {
		public Prepared {
			byStructure = Map.copyOf(byStructure);
		}
	}

	private record PreparedDefinition(WorldMapPoiDefinition definition, Identifier structureId) {
	}
}

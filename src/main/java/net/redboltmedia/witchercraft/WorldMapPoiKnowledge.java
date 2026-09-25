package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Server-owned, per-player POI reveal and discovery knowledge. */
public final class WorldMapPoiKnowledge extends SavedData {
	public static final int FORMAT_VERSION = 2;
	public static final int MAX_PLAYERS = 65_536;
	public static final int MAX_KNOWLEDGE_PER_PLAYER = 16_384;

	private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map/poi_knowledge");
	private static final Codec<WorldMapPoiKnowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("format_version", FORMAT_VERSION).forGetter(ignored -> FORMAT_VERSION),
		StoredPlayer.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(WorldMapPoiKnowledge::storedPlayers)
	).apply(instance, WorldMapPoiKnowledge::new));
	public static final SavedDataType<WorldMapPoiKnowledge> TYPE = new SavedDataType<>(DATA_ID, WorldMapPoiKnowledge::new, CODEC);

	private final Map<UUID, Map<UUID, Entry>> knowledgeByPlayer = new LinkedHashMap<>();

	public WorldMapPoiKnowledge() {
	}

	private WorldMapPoiKnowledge(int formatVersion, List<StoredPlayer> players) {
		if (formatVersion != FORMAT_VERSION)
			WitchercraftMod.LOGGER.warn("Loading POI knowledge data version {} with reader version {}", formatVersion, FORMAT_VERSION);
		for (StoredPlayer player : players) {
			if (knowledgeByPlayer.size() >= MAX_PLAYERS) {
				WitchercraftMod.LOGGER.warn("Discarded POI player knowledge beyond the limit of {}", MAX_PLAYERS);
				break;
			}
			loadPlayer(player);
		}
	}

	public static WorldMapPoiKnowledge get(MinecraftServer server) {
		requireServerThread(server);
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public @Nullable Entry get(UUID playerId, UUID markerId) {
		Map<UUID, Entry> playerKnowledge = knowledgeByPlayer.get(playerId);
		return playerKnowledge == null ? null : playerKnowledge.get(markerId);
	}

	public List<Entry> entries(UUID playerId) {
		Map<UUID, Entry> playerKnowledge = knowledgeByPlayer.get(playerId);
		return playerKnowledge == null ? List.of() : List.copyOf(playerKnowledge.values());
	}

	public boolean reveal(UUID playerId, UUID markerId, double offsetX, double offsetZ) {
		if (!validOffset(offsetX, offsetZ))
			return false;
		Map<UUID, Entry> playerKnowledge = knowledgeByPlayer.get(playerId);
		if (playerKnowledge == null) {
			if (knowledgeByPlayer.size() >= MAX_PLAYERS)
				return false;
			playerKnowledge = new LinkedHashMap<>();
			knowledgeByPlayer.put(playerId, playerKnowledge);
		}
		if (playerKnowledge.containsKey(markerId) || playerKnowledge.size() >= MAX_KNOWLEDGE_PER_PLAYER)
			return false;
		playerKnowledge.put(markerId, new Entry(markerId, newPresentationId(playerKnowledge), State.REVEALED, offsetX, offsetZ));
		setDirty();
		return true;
	}

	public boolean discover(UUID playerId, UUID markerId) {
		Map<UUID, Entry> playerKnowledge = knowledgeByPlayer.get(playerId);
		if (playerKnowledge == null) {
			if (knowledgeByPlayer.size() >= MAX_PLAYERS)
				return false;
			playerKnowledge = new LinkedHashMap<>();
			knowledgeByPlayer.put(playerId, playerKnowledge);
		}
		Entry previous = playerKnowledge.get(markerId);
		if (previous != null && previous.state() == State.DISCOVERED)
			return false;
		if (previous == null && playerKnowledge.size() >= MAX_KNOWLEDGE_PER_PLAYER)
			return false;
		playerKnowledge.put(markerId, previous == null ? new Entry(markerId, newPresentationId(playerKnowledge), State.DISCOVERED, 0.0, 0.0)
			: new Entry(markerId, previous.presentationId(), State.DISCOVERED, previous.offsetX(), previous.offsetZ()));
		setDirty();
		return true;
	}

	/**
	 * Removes every player's knowledge of a deleted POI and returns each affected player's
	 * presentation UUID, so connected clients can drop the marker from their caches.
	 */
	public Map<UUID, UUID> forget(UUID markerId) {
		Map<UUID, UUID> removed = new LinkedHashMap<>();
		for (Map.Entry<UUID, Map<UUID, Entry>> player : knowledgeByPlayer.entrySet()) {
			Entry entry = player.getValue().remove(markerId);
			if (entry != null)
				removed.put(player.getKey(), entry.presentationId());
		}
		if (!removed.isEmpty())
			setDirty();
		return removed;
	}

	private void loadPlayer(StoredPlayer storedPlayer) {
		UUID playerId;
		try {
			playerId = UUID.fromString(storedPlayer.playerId());
		} catch (IllegalArgumentException exception) {
			WitchercraftMod.LOGGER.warn("Discarded POI knowledge with invalid player UUID");
			return;
		}
		if (knowledgeByPlayer.containsKey(playerId)) {
			WitchercraftMod.LOGGER.warn("Discarded duplicate POI knowledge collection for player {}", playerId);
			return;
		}
		Map<UUID, Entry> loaded = new LinkedHashMap<>();
		Set<UUID> presentationIdentifiers = new HashSet<>();
		boolean migrated = false;
		for (StoredEntry stored : storedPlayer.entries()) {
			if (loaded.size() >= MAX_KNOWLEDGE_PER_PLAYER)
				break;
			Entry decoded = stored.decode();
			if (decoded == null || !presentationIdentifiers.add(decoded.presentationId()) || loaded.putIfAbsent(decoded.markerId(), decoded) != null)
				WitchercraftMod.LOGGER.warn("Discarded invalid or duplicate POI knowledge for player {}", playerId);
			else if (stored.presentationId().isBlank())
				migrated = true;
		}
		if (!loaded.isEmpty())
			knowledgeByPlayer.put(playerId, loaded);
		if (migrated)
			setDirty();
	}

	private List<StoredPlayer> storedPlayers() {
		return knowledgeByPlayer.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(UUID::toString)))
			.map(entry -> new StoredPlayer(entry.getKey().toString(), entry.getValue().values().stream()
				.sorted(Comparator.comparing(value -> value.markerId().toString())).map(StoredEntry::from).toList())).toList();
	}

	private static boolean validOffset(double x, double z) {
		return Double.isFinite(x) && Double.isFinite(z) && Math.abs(x) <= WorldMapPoiDefinition.MAX_RADIUS && Math.abs(z) <= WorldMapPoiDefinition.MAX_RADIUS;
	}

	private static UUID newPresentationId(Map<UUID, Entry> playerKnowledge) {
		UUID candidate;
		do {
			candidate = UUID.randomUUID();
		} while (containsPresentationId(playerKnowledge, candidate));
		return candidate;
	}

	private static boolean containsPresentationId(Map<UUID, Entry> playerKnowledge, UUID candidate) {
		for (Entry entry : playerKnowledge.values())
			if (entry.presentationId().equals(candidate))
				return true;
		return false;
	}

	private static void requireServerThread(MinecraftServer server) {
		if (server == null || !server.isSameThread())
			throw new IllegalStateException("World-map POI knowledge may only be accessed on the server thread");
	}

	public enum State {
		REVEALED, DISCOVERED;

		private static @Nullable State byId(String id) {
			try {
				return valueOf(id.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException | NullPointerException exception) {
				return null;
			}
		}
	}

	public record Entry(UUID markerId, UUID presentationId, State state, double offsetX, double offsetZ) {
	}

	private record StoredPlayer(String playerId, List<StoredEntry> entries) {
		private static final Codec<StoredPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("player_id", "").forGetter(StoredPlayer::playerId),
			StoredEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(StoredPlayer::entries)
		).apply(instance, StoredPlayer::new));
	}

	private record StoredEntry(String markerId, String presentationId, String state, double offsetX, double offsetZ) {
		private static final Codec<StoredEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("marker_id", "").forGetter(StoredEntry::markerId),
			Codec.STRING.optionalFieldOf("presentation_id", "").forGetter(StoredEntry::presentationId),
			Codec.STRING.optionalFieldOf("state", "").forGetter(StoredEntry::state),
			Codec.DOUBLE.optionalFieldOf("offset_x", 0.0).forGetter(StoredEntry::offsetX),
			Codec.DOUBLE.optionalFieldOf("offset_z", 0.0).forGetter(StoredEntry::offsetZ)
		).apply(instance, StoredEntry::new));

		private static StoredEntry from(Entry value) {
			return new StoredEntry(value.markerId().toString(), value.presentationId().toString(),
				value.state().name().toLowerCase(Locale.ROOT), value.offsetX(), value.offsetZ());
		}

		private @Nullable Entry decode() {
			try {
				UUID parsedMarkerId = UUID.fromString(markerId);
				UUID parsedPresentationId = presentationId.isBlank() ? UUID.randomUUID() : UUID.fromString(presentationId);
				State parsedState = State.byId(state);
				if (parsedState == null || !validOffset(offsetX, offsetZ))
					return null;
				return new Entry(parsedMarkerId, parsedPresentationId, parsedState, offsetX, offsetZ);
			} catch (IllegalArgumentException exception) {
				return null;
			}
		}
	}
}

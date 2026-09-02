package net.redboltmedia.witchercraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Server-owned personal waypoint storage and mutation validation. */
public final class WorldMapWaypoints extends SavedData {
	public static final int FORMAT_VERSION = 2;
	public static final int MAX_WAYPOINTS_PER_PLAYER = 200;
	public static final int MAX_NAME_CHARACTERS = 64;
	public static final double MAX_ABSOLUTE_COORDINATE = 30_000_000.0;

	private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map/waypoints");
	private static final Codec<WorldMapWaypoints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("format_version", FORMAT_VERSION).forGetter(ignored -> FORMAT_VERSION),
		PlayerEntry.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(WorldMapWaypoints::storedPlayers)
	).apply(instance, WorldMapWaypoints::new));
	public static final SavedDataType<WorldMapWaypoints> TYPE = new SavedDataType<>(DATA_ID, WorldMapWaypoints::new, CODEC);

	private final Map<UUID, List<Waypoint>> waypointsByPlayer = new HashMap<>();

	public WorldMapWaypoints() {
	}

	private WorldMapWaypoints(int formatVersion, List<PlayerEntry> players) {
		if (formatVersion > FORMAT_VERSION)
			WitchercraftMod.LOGGER.warn("Loading newer waypoint data version {} with reader version {}", formatVersion, FORMAT_VERSION);
		for (PlayerEntry player : players)
			loadPlayer(player);
	}

	public static WorldMapWaypoints get(MinecraftServer server) {
		requireServerThread(server);
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public List<Waypoint> getWaypoints(ServerPlayer player) {
		requireServerThread(player.level().getServer());
		return List.copyOf(waypointsByPlayer.getOrDefault(player.getUUID(), List.of()));
	}

	public OperationResult create(ServerPlayer player, Identifier dimension, double x, double z, String name, WaypointIcon icon) {
		requireServerThread(player.level().getServer());
		List<Waypoint> waypoints = waypointsByPlayer.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>());
		if (waypoints.size() >= MAX_WAYPOINTS_PER_PLAYER)
			return OperationResult.failure(Status.LIMIT_REACHED);
		String normalizedName = normalizeName(name);
		Status validation = validateInput(player.level().getServer(), dimension, x, z, normalizedName, icon);
		if (validation != Status.SUCCESS)
			return OperationResult.failure(validation);
		UUID id;
		do id = UUID.randomUUID(); while (find(waypoints, id) != null);
		Waypoint waypoint = new Waypoint(id, dimension, x, z, normalizedName, icon, true);
		waypoints.add(waypoint);
		setDirty();
		return OperationResult.success(waypoint);
	}

	public OperationResult edit(ServerPlayer player, UUID id, String name, WaypointIcon icon) {
		requireServerThread(player.level().getServer());
		List<Waypoint> waypoints = waypointsByPlayer.get(player.getUUID());
		Waypoint old = find(waypoints, id);
		if (old == null)
			return OperationResult.failure(Status.NOT_FOUND);
		String normalizedName = normalizeName(name);
		Status validation = validateInput(player.level().getServer(), old.dimension(), old.x(), old.z(), normalizedName, icon);
		if (validation != Status.SUCCESS)
			return OperationResult.failure(validation);
		Waypoint replacement = new Waypoint(old.id(), old.dimension(), old.x(), old.z(), normalizedName, icon, old.visible());
		replace(waypoints, replacement);
		setDirty();
		return OperationResult.success(replacement);
	}

	public OperationResult setVisible(ServerPlayer player, UUID id, boolean visible) {
		requireServerThread(player.level().getServer());
		List<Waypoint> waypoints = waypointsByPlayer.get(player.getUUID());
		Waypoint old = find(waypoints, id);
		if (old == null)
			return OperationResult.failure(Status.NOT_FOUND);
		Waypoint replacement = new Waypoint(old.id(), old.dimension(), old.x(), old.z(), old.name(), old.icon(), visible);
		replace(waypoints, replacement);
		setDirty();
		return OperationResult.success(replacement);
	}

	public OperationResult delete(ServerPlayer player, UUID id) {
		requireServerThread(player.level().getServer());
		List<Waypoint> waypoints = waypointsByPlayer.get(player.getUUID());
		Waypoint old = find(waypoints, id);
		if (old == null)
			return OperationResult.failure(Status.NOT_FOUND);
		waypoints.remove(old);
		if (waypoints.isEmpty())
			waypointsByPlayer.remove(player.getUUID());
		setDirty();
		return OperationResult.success(old);
	}

	private void loadPlayer(PlayerEntry storedPlayer) {
		UUID playerId;
		try {
			playerId = UUID.fromString(storedPlayer.playerId());
		} catch (IllegalArgumentException exception) {
			WitchercraftMod.LOGGER.warn("Discarded waypoint collection with invalid player UUID");
			return;
		}
		List<Waypoint> loaded = new ArrayList<>();
		Set<UUID> identifiers = new HashSet<>();
		for (StoredWaypoint stored : storedPlayer.waypoints()) {
			if (loaded.size() >= MAX_WAYPOINTS_PER_PLAYER)
				break;
			Waypoint waypoint = stored.decode();
			if (waypoint == null || !identifiers.add(waypoint.id()) || !validStoredWaypoint(waypoint)) {
				WitchercraftMod.LOGGER.warn("Discarded invalid personal waypoint for player {}", playerId);
				continue;
			}
			loaded.add(waypoint);
		}
		if (!loaded.isEmpty())
			waypointsByPlayer.put(playerId, loaded);
	}

	private List<PlayerEntry> storedPlayers() {
		return waypointsByPlayer.entrySet().stream().map(entry -> new PlayerEntry(entry.getKey().toString(), entry.getValue().stream().map(StoredWaypoint::from).toList())).toList();
	}

	private static Status validateInput(MinecraftServer server, Identifier dimension, double x, double z, String name, WaypointIcon icon) {
		if (dimension == null || icon == null || !validName(name) || !validCoordinates(x, z))
			return Status.INVALID_INPUT;
		ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
		if (level == null)
			return Status.INVALID_DIMENSION;
		var border = level.getWorldBorder();
		if (x < border.getMinX() || x > border.getMaxX() || z < border.getMinZ() || z > border.getMaxZ())
			return Status.OUTSIDE_WORLD_BORDER;
		return Status.SUCCESS;
	}

	private static boolean validStoredWaypoint(Waypoint waypoint) {
		return waypoint.dimension() != null && waypoint.icon() != null && validName(waypoint.name()) && validCoordinates(waypoint.x(), waypoint.z());
	}

	private static boolean validCoordinates(double x, double z) {
		return Double.isFinite(x) && Double.isFinite(z) && Math.abs(x) <= MAX_ABSOLUTE_COORDINATE && Math.abs(z) <= MAX_ABSOLUTE_COORDINATE;
	}

	private static boolean validName(String name) {
		if (name == null || name.isBlank() || name.codePointCount(0, name.length()) > MAX_NAME_CHARACTERS)
			return false;
		return name.codePoints().noneMatch(Character::isISOControl);
	}

	private static String normalizeName(@Nullable String name) {
		return name == null ? "" : name.strip();
	}

	private static @Nullable Waypoint find(@Nullable List<Waypoint> waypoints, UUID id) {
		if (waypoints == null || id == null)
			return null;
		for (Waypoint waypoint : waypoints)
			if (waypoint.id().equals(id))
				return waypoint;
		return null;
	}

	private static void replace(List<Waypoint> waypoints, Waypoint replacement) {
		for (int i = 0; i < waypoints.size(); i++) {
			if (waypoints.get(i).id().equals(replacement.id())) {
				waypoints.set(i, replacement);
				return;
			}
		}
	}

	private static void requireServerThread(MinecraftServer server) {
		if (server == null || !server.isSameThread())
			throw new IllegalStateException("Personal waypoint data may only be accessed on the server thread");
	}

	public enum WaypointIcon {
		HOME, CAMP, CHEST, DANGER, HERB, MONSTER, QUEST;

		public String id() {
			return name().toLowerCase(Locale.ROOT);
		}

		public int atlasIndex() {
			return ordinal() + 1;
		}

		public static @Nullable WaypointIcon byId(String id) {
			try {
				return valueOf(id.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException | NullPointerException exception) {
				return null;
			}
		}
	}

	public record Waypoint(UUID id, Identifier dimension, double x, double z, String name, WaypointIcon icon, boolean visible) {
	}

	public enum Status {
		SUCCESS, NOT_FOUND, LIMIT_REACHED, INVALID_INPUT, INVALID_DIMENSION, OUTSIDE_WORLD_BORDER
	}

	public record OperationResult(Status status, @Nullable Waypoint waypoint) {
		private static OperationResult success(Waypoint waypoint) {
			return new OperationResult(Status.SUCCESS, waypoint);
		}

		private static OperationResult failure(Status status) {
			return new OperationResult(status, null);
		}
	}

	private record PlayerEntry(String playerId, List<StoredWaypoint> waypoints) {
		private static final Codec<PlayerEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("player_id").forGetter(PlayerEntry::playerId),
			StoredWaypoint.CODEC.listOf().optionalFieldOf("waypoints", List.of()).forGetter(PlayerEntry::waypoints)
		).apply(instance, PlayerEntry::new));
	}

	private record StoredWaypoint(String id, String dimension, double x, double z, String name, boolean visible, String legacyIcon, String legacyColor, boolean legacyTracked) {
		private static final Codec<StoredWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(StoredWaypoint::id),
			Codec.STRING.fieldOf("dimension").forGetter(StoredWaypoint::dimension),
			Codec.DOUBLE.fieldOf("x").forGetter(StoredWaypoint::x),
			Codec.DOUBLE.fieldOf("z").forGetter(StoredWaypoint::z),
			Codec.STRING.fieldOf("name").forGetter(StoredWaypoint::name),
			Codec.BOOL.optionalFieldOf("visible", true).forGetter(StoredWaypoint::visible),
			Codec.STRING.optionalFieldOf("icon", "home").forGetter(StoredWaypoint::legacyIcon),
			Codec.STRING.optionalFieldOf("color", "gold").forGetter(StoredWaypoint::legacyColor),
			Codec.BOOL.optionalFieldOf("tracked", false).forGetter(StoredWaypoint::legacyTracked)
		).apply(instance, StoredWaypoint::new));

		private static StoredWaypoint from(Waypoint waypoint) {
			return new StoredWaypoint(waypoint.id().toString(), waypoint.dimension().toString(), waypoint.x(), waypoint.z(), waypoint.name(), waypoint.visible(), waypoint.icon().id(), "gold", false);
		}

		private @Nullable Waypoint decode() {
			try {
				UUID parsedId = UUID.fromString(id);
				Identifier parsedDimension = Identifier.tryParse(dimension);
				WaypointIcon parsedIcon = WaypointIcon.byId(legacyIcon);
				if (parsedDimension == null || parsedIcon == null)
					return null;
				return new Waypoint(parsedId, parsedDimension, x, z, normalizeName(name), parsedIcon, visible);
			} catch (IllegalArgumentException exception) {
				return null;
			}
		}
	}
}

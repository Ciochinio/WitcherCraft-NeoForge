package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Connection-scoped client copy of the server's personal waypoint state. */
public final class WorldMapWaypointClientCache {
	private static final UUID NO_ID = new UUID(0L, 0L);
	private static final int MAX_REMEMBERED_RESULTS = 32;
	private static final Map<Integer, WorldMapWaypointResultMessage> RESULTS = new LinkedHashMap<>();
	private static final Map<Identifier, TemporaryPin> TEMPORARY_PINS = new LinkedHashMap<>();
	private static List<WorldMapWaypoints.Waypoint> waypoints = List.of();
	private static Object connectionIdentity;
	private static int nextRequestId = 1;
	private static boolean synchronizedWithServer;

	private WorldMapWaypointClientCache() {
	}

	public static int requestSnapshot() {
		return send(WorldMapWaypointMutationMessage.Operation.REQUEST_SNAPSHOT, NO_ID, "", 0, 0, "", "", false);
	}

	public static int create(Identifier dimension, double x, double z, String name, WorldMapWaypoints.WaypointIcon icon) {
		return send(WorldMapWaypointMutationMessage.Operation.CREATE, NO_ID, dimension.toString(), x, z, name, icon.id(), false);
	}

	public static int edit(UUID id, String name, WorldMapWaypoints.WaypointIcon icon) {
		return send(WorldMapWaypointMutationMessage.Operation.EDIT, id, "", 0, 0, name, icon.id(), false);
	}

	public static int setVisible(UUID id, boolean visible) {
		return send(WorldMapWaypointMutationMessage.Operation.SET_VISIBLE, id, "", 0, 0, "", "", visible);
	}

	public static int delete(UUID id) {
		return send(WorldMapWaypointMutationMessage.Operation.DELETE, id, "", 0, 0, "", "", false);
	}

	public static List<WorldMapWaypoints.Waypoint> waypoints() {
		ensureConnection();
		return waypoints;
	}

	public static List<WorldMapWaypoints.Waypoint> waypoints(Identifier dimension) {
		ensureConnection();
		List<WorldMapWaypoints.Waypoint> matching = new ArrayList<>();
		for (WorldMapWaypoints.Waypoint waypoint : waypoints)
			if (waypoint.dimension().equals(dimension))
				matching.add(waypoint);
		return List.copyOf(matching);
	}

	public static boolean isSynchronized() {
		ensureConnection();
		return synchronizedWithServer;
	}

	public static WorldMapWaypointResultMessage takeResult(int requestId) {
		ensureConnection();
		return RESULTS.remove(requestId);
	}

	public static void placeTemporaryPin(Identifier dimension, double x, double z) {
		ensureConnection();
		if (Minecraft.getInstance().getConnection() != null && dimension != null && Double.isFinite(x) && Double.isFinite(z))
			TEMPORARY_PINS.put(dimension, new TemporaryPin(dimension, x, z));
	}

	public static TemporaryPin temporaryPin(Identifier dimension) {
		ensureConnection();
		return TEMPORARY_PINS.get(dimension);
	}

	static void acceptSnapshot(List<WorldMapWaypoints.Waypoint> snapshot) {
		ensureConnection();
		if (Minecraft.getInstance().getConnection() == null)
			return;
		waypoints = List.copyOf(snapshot);
		synchronizedWithServer = true;
	}

	static void acceptResult(WorldMapWaypointResultMessage result) {
		ensureConnection();
		if (Minecraft.getInstance().getConnection() == null)
			return;
		RESULTS.put(result.requestId(), result);
		while (RESULTS.size() > MAX_REMEMBERED_RESULTS)
			RESULTS.remove(RESULTS.keySet().iterator().next());
	}

	public static void clear() {
		waypoints = List.of();
		RESULTS.clear();
		TEMPORARY_PINS.clear();
		synchronizedWithServer = false;
		nextRequestId = 1;
	}

	public record TemporaryPin(Identifier dimension, double x, double z) {
	}

	private static int send(WorldMapWaypointMutationMessage.Operation operation, UUID id, String dimension, double x, double z, String name, String icon, boolean value) {
		ensureConnection();
		if (Minecraft.getInstance().getConnection() == null)
			return 0;
		int requestId = nextRequestId++;
		if (nextRequestId <= 0)
			nextRequestId = 1;
		ClientPacketDistributor.sendToServer(new WorldMapWaypointMutationMessage(requestId, operation, id, dimension, x, z, name, icon, value));
		return requestId;
	}

	private static void ensureConnection() {
		Object current = Minecraft.getInstance().getConnection();
		if (connectionIdentity != current) {
			clear();
			connectionIdentity = current;
		}
	}
}

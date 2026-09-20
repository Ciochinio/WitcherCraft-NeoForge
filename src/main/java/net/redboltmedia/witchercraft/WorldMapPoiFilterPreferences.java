package net.redboltmedia.witchercraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Client-owned map filter preferences, isolated by the server-issued world UUID. */
public final class WorldMapPoiFilterPreferences {
	private static final int FORMAT_VERSION = 1;
	private static final int MAX_WORLDS = 128;
	private static final long MAX_FILE_BYTES = 262_144L;
	private static final UUID NO_WORLD = new UUID(0L, 0L);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<UUID, Preferences> WORLDS = new LinkedHashMap<>();
	private static boolean loaded;

	private WorldMapPoiFilterPreferences() {
	}

	public static boolean personalWaypoints(UUID worldId) {
		Preferences preferences = get(worldId, false);
		return preferences == null || preferences.personalWaypoints;
	}

	public static boolean poiVisible(UUID worldId, WorldMapPoiMarker marker) {
		Preferences preferences = get(worldId, false);
		Boolean override = preferences == null ? null : marker instanceof WorldMapPoiMarker.Unknown
			? preferences.unknownPois : preferences.discoveredPois;
		return override == null ? marker.defaultVisible() : override;
	}

	public static DisplayState state(UUID worldId, Filter filter) {
		Preferences preferences = get(worldId, false);
		if (filter == Filter.PERSONAL_WAYPOINTS)
			return preferences == null || preferences.personalWaypoints ? DisplayState.SHOWN : DisplayState.HIDDEN;
		Boolean value = preferences == null ? null : filter == Filter.UNKNOWN_POIS ? preferences.unknownPois : preferences.discoveredPois;
		return value == null ? DisplayState.DEFAULT : value ? DisplayState.SHOWN : DisplayState.HIDDEN;
	}

	public static void toggle(UUID worldId, Filter filter, boolean defaultShown) {
		Preferences preferences = get(worldId, true);
		if (preferences == null)
			return;
		switch (filter) {
			case PERSONAL_WAYPOINTS -> preferences.personalWaypoints = !preferences.personalWaypoints;
			case UNKNOWN_POIS -> preferences.unknownPois = preferences.unknownPois == null ? !defaultShown : !preferences.unknownPois;
			case DISCOVERED_POIS -> preferences.discoveredPois = preferences.discoveredPois == null ? !defaultShown : !preferences.discoveredPois;
		}
		save();
	}

	private static Preferences get(UUID worldId, boolean create) {
		load();
		if (worldId == null || NO_WORLD.equals(worldId))
			return null;
		Preferences preferences = WORLDS.get(worldId);
		if (preferences == null && create) {
			preferences = new Preferences();
			WORLDS.put(worldId, preferences);
			trim();
		}
		return preferences;
	}

	private static void load() {
		if (loaded)
			return;
		loaded = true;
		Path path = path();
		try {
			if (!Files.isRegularFile(path) || Files.size(path) > MAX_FILE_BYTES)
				return;
		} catch (IOException exception) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			if (!root.has("version") || root.get("version").getAsInt() != FORMAT_VERSION || !root.has("worlds") || !root.get("worlds").isJsonObject())
				return;
			int accepted = 0;
			for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("worlds").entrySet()) {
				if (accepted >= MAX_WORLDS || !entry.getValue().isJsonObject())
					break;
				try {
					UUID id = UUID.fromString(entry.getKey());
					if (NO_WORLD.equals(id))
						continue;
					JsonObject value = entry.getValue().getAsJsonObject();
					Preferences preferences = new Preferences();
					if (value.has("personal_waypoints") && value.get("personal_waypoints").isJsonPrimitive())
						preferences.personalWaypoints = value.get("personal_waypoints").getAsBoolean();
					preferences.unknownPois = optionalBoolean(value, "unknown_pois");
					preferences.discoveredPois = optionalBoolean(value, "discovered_pois");
					WORLDS.put(id, preferences);
					accepted++;
				} catch (RuntimeException exception) {
					WitchercraftMod.LOGGER.warn("Skipping malformed world-map filter preferences for {}", entry.getKey());
				}
			}
		} catch (Exception exception) {
			WitchercraftMod.LOGGER.warn("Could not read world-map filter preferences", exception);
		}
	}

	private static Boolean optionalBoolean(JsonObject object, String name) {
		return object.has(name) && object.get(name).isJsonPrimitive() ? object.get(name).getAsBoolean() : null;
	}

	private static void save() {
		JsonObject root = new JsonObject();
		root.addProperty("version", FORMAT_VERSION);
		JsonObject worlds = new JsonObject();
		for (Map.Entry<UUID, Preferences> entry : WORLDS.entrySet()) {
			JsonObject value = new JsonObject();
			value.addProperty("personal_waypoints", entry.getValue().personalWaypoints);
			if (entry.getValue().unknownPois != null)
				value.addProperty("unknown_pois", entry.getValue().unknownPois);
			if (entry.getValue().discoveredPois != null)
				value.addProperty("discovered_pois", entry.getValue().discoveredPois);
			worlds.add(entry.getKey().toString(), value);
		}
		root.add("worlds", worlds);
		Path target = path();
		Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
		try {
			Files.createDirectories(target.getParent());
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(root, writer);
			}
			try {
				Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException exception) {
			WitchercraftMod.LOGGER.warn("Could not save world-map filter preferences", exception);
		}
	}

	private static void trim() {
		Iterator<UUID> iterator = WORLDS.keySet().iterator();
		while (WORLDS.size() > MAX_WORLDS && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	private static Path path() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("witchercraft-world-map-filters.json");
	}

	public enum Filter {
		PERSONAL_WAYPOINTS, UNKNOWN_POIS, DISCOVERED_POIS
	}

	public enum DisplayState {
		DEFAULT, SHOWN, HIDDEN
	}

	private static final class Preferences {
		private boolean personalWaypoints = true;
		private Boolean unknownPois;
		private Boolean discoveredPois;
	}
}

package net.redboltmedia.witchercraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * HAND-MAINTAINED (locked code element, ~/World Map/POI). Client-side translation of generated place
 * names, such as village signpost names.
 *
 * The world setting "Map name language" can fix the language of place names for every player on a
 * server. When it is set, the client reads that language's file from its own resources (every client
 * has all of the mod's language files) instead of using its game language. Missing entries fall back
 * to English, then to the server-supplied fallback (the sign's coordinates). Interface text is never
 * affected. Only call this from client code.
 */
public final class WorldMapPlaceNames {
	private static final String FALLBACK_LANGUAGE = "en_us";
	private static final Map<String, Map<String, String>> TABLES = new HashMap<>();

	private WorldMapPlaceNames() {
	}

	/** The display text of a place name: the translated key, else {@code fallback}. */
	public static String resolve(String nameKey, String fallback) {
		String safeFallback = fallback == null ? "" : fallback;
		if (nameKey == null || nameKey.isEmpty())
			return safeFallback;
		String language = WorldMapServerConfig.mapNameLanguage();
		if (language.isEmpty()) {
			Language current = Language.getInstance();
			return current.has(nameKey) ? current.getOrDefault(nameKey) : safeFallback;
		}
		String text = table(language).get(nameKey);
		if (text == null && !FALLBACK_LANGUAGE.equals(language))
			text = table(FALLBACK_LANGUAGE).get(nameKey);
		return text == null ? safeFallback : text;
	}

	/** Forgets loaded language files, for example after resource packs change. */
	public static synchronized void clear() {
		TABLES.clear();
	}

	private static synchronized Map<String, String> table(String language) {
		return TABLES.computeIfAbsent(language, WorldMapPlaceNames::load);
	}

	/** Merges this mod's file for {@code language} from every resource pack, higher packs winning. */
	private static Map<String, String> load(String language) {
		Map<String, String> entries = new HashMap<>();
		Identifier file = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "lang/" + language + ".json");
		for (Resource resource : Minecraft.getInstance().getResourceManager().getResourceStack(file)) {
			try (Reader reader = resource.openAsReader()) {
				JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : object.entrySet())
					if (entry.getKey().startsWith(FastTravelVillageSigns.NAME_KEY_PREFIX) && entry.getValue().isJsonPrimitive())
						entries.put(entry.getKey(), entry.getValue().getAsString());
			} catch (Exception exception) {
				WitchercraftMod.LOGGER.warn("Could not read place names from {} in {}", file, resource.sourcePackId(), exception);
			}
		}
		return entries;
	}
}

package net.redboltmedia.witchercraft;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The client's copy of what it may know about alchemy recipes:
 *
 * - the ingredient allowlist and the recipe index (id, category, result of
 *   every recipe) and the knowledge mode, from {@link AlchemyIndexSyncMessage};
 * - the full contents of KNOWN recipes only, from {@link AlchemyKnownSyncMessage}.
 *   Unknown recipes' bases and ingredients never reach the client.
 *
 * Plain static state with no client-only types, so the common menu class may
 * reference it. Written and read on the client main thread (packet handlers
 * enqueue their work). Empty until the first sync, which arrives on login.
 *
 * HAND-MAINTAINED: locked code element.
 */
public final class AlchemyClientIndex {
	private AlchemyClientIndex() {
	}

	private static volatile Set<Item> ingredientItems = Set.of();
	private static List<AlchemyIndexSyncMessage.Entry> recipes = List.of();
	private static AlchemyKnowledge.Mode mode = AlchemyKnowledge.Mode.DISCOVERY;
	private static final Map<Identifier, AlchemyRecipe> known = new LinkedHashMap<>();

	// ---- index ------------------------------------------------------------------------

	public static void setIndex(Collection<Item> items, List<AlchemyIndexSyncMessage.Entry> entries, AlchemyKnowledge.Mode newMode) {
		ingredientItems = Set.copyOf(items);
		recipes = List.copyOf(entries);
		mode = newMode;
	}

	public static boolean isIngredient(ItemStack stack) {
		return !stack.isEmpty() && ingredientItems.contains(stack.getItem());
	}

	/** Every recipe's id, category and result, sorted by id. */
	public static List<AlchemyIndexSyncMessage.Entry> recipes() {
		return recipes;
	}

	public static AlchemyKnowledge.Mode mode() {
		return mode;
	}

	/** The result item of any recipe (known or not), or null if the id is unknown to the index. */
	public static Item resultOf(Identifier id) {
		for (AlchemyIndexSyncMessage.Entry entry : recipes)
			if (entry.id().equals(id))
				return entry.result();
		return null;
	}

	// ---- known recipes ----------------------------------------------------------------

	public static void setKnown(List<AlchemyKnownSyncMessage.Entry> entries) {
		known.clear();
		addKnown(entries);
	}

	public static void addKnown(List<AlchemyKnownSyncMessage.Entry> entries) {
		for (AlchemyKnownSyncMessage.Entry entry : entries)
			known.put(entry.id(), entry.recipe());
	}

	/** The full contents of a known recipe, or null if the player does not know it. */
	public static AlchemyRecipe known(Identifier id) {
		return known.get(id);
	}

	public static boolean isKnown(Identifier id) {
		return known.containsKey(id);
	}
}

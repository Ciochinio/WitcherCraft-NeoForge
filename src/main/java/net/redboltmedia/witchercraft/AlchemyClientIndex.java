package net.redboltmedia.witchercraft;

import java.util.Collection;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The client's copy of what it needs to know about alchemy recipes, sent by
 * {@link AlchemyIndexSyncMessage}. Today that is only the ingredient allowlist,
 * so the client's alchemy slots accept exactly the items the server's do. It
 * never holds recipe contents (see the plan's rule that unknown recipes are not
 * sent to the client).
 *
 * Plain static state with no client-only types, so the common menu class may
 * reference it. Empty until the first sync, which arrives on login.
 *
 * HAND-MAINTAINED: locked code element.
 */
public final class AlchemyClientIndex {
	private AlchemyClientIndex() {
	}

	private static volatile Set<Item> ingredientItems = Set.of();

	public static void setIngredientItems(Collection<Item> items) {
		ingredientItems = Set.copyOf(items);
	}

	public static boolean isIngredient(ItemStack stack) {
		return !stack.isEmpty() && ingredientItems.contains(stack.getItem());
	}
}

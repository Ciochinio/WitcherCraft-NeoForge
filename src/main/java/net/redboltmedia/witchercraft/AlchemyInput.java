package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * What an alchemy recipe is matched against: the base slot and the five
 * ingredient slots of the alchemy circle. Index 0 is the base; 1 to 5 are the
 * ingredients in slot order (order does not matter to matching).
 *
 * HAND-MAINTAINED: locked code element.
 */
public record AlchemyInput(ItemStack base, List<ItemStack> ingredients) implements RecipeInput {

	@Override
	public ItemStack getItem(int index) {
		return index == 0 ? base : ingredients.get(index - 1);
	}

	@Override
	public int size() {
		return 1 + ingredients.size();
	}

	/** The ingredient stacks that are not empty. */
	public List<ItemStack> filledIngredients() {
		List<ItemStack> filled = new ArrayList<>(ingredients.size());
		for (ItemStack stack : ingredients)
			if (!stack.isEmpty())
				filled.add(stack);
		return filled;
	}
}

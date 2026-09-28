package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The picture part of a recipe book entry's tooltip: the base, a small gap, then
 * each ingredient, as item icons. A tag ingredient cycles through its items
 * once a second, like vanilla's ghost items. The factory is registered by
 * {@link AlchemyRecipeBook}.
 *
 * The record is common (a TooltipComponent); {@link Client} is client only.
 *
 * HAND-MAINTAINED: locked code element.
 */
public record AlchemyRecipeTooltip(Ingredient base, List<Ingredient> ingredients) implements TooltipComponent {
	private static final int ICON = 18;
	private static final int BASE_GAP = 6;

	public static AlchemyRecipeTooltip of(AlchemyRecipe recipe) {
		return new AlchemyRecipeTooltip(recipe.base(), recipe.ingredients());
	}

	/** Every item an ingredient accepts, as stacks, for cycling. */
	@SuppressWarnings("deprecation")
	public static List<ItemStack> stacksOf(Ingredient ingredient) {
		List<ItemStack> stacks = new ArrayList<>();
		ingredient.items().map(Holder::value).forEach((Item item) -> stacks.add(new ItemStack(item)));
		return stacks;
	}

	/** The stack to show now for a cycling ingredient. */
	public static ItemStack cycled(List<ItemStack> stacks) {
		return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get((int) (Util.getMillis() / 1000L % stacks.size()));
	}

	public static final class Client implements ClientTooltipComponent {
		private final List<ItemStack> base;
		private final List<List<ItemStack>> ingredients = new ArrayList<>();

		public Client(AlchemyRecipeTooltip tooltip) {
			this.base = stacksOf(tooltip.base());
			for (Ingredient ingredient : tooltip.ingredients())
				this.ingredients.add(stacksOf(ingredient));
		}

		@Override
		public int getHeight(Font font) {
			return ICON + 2;
		}

		@Override
		public int getWidth(Font font) {
			return ICON + BASE_GAP + ingredients.size() * ICON;
		}

		@Override
		public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
			graphics.fakeItem(cycled(base), x + 1, y + 1);
			for (int i = 0; i < ingredients.size(); i++)
				graphics.fakeItem(cycled(ingredients.get(i)), x + ICON + BASE_GAP + i * ICON + 1, y + 1);
		}
	}
}

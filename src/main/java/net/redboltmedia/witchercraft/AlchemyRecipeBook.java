package net.redboltmedia.witchercraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The recipe book panel of the Alchemy tab, drawn by {@link AlchemyScreen} to
 * the left of the slot block: a manuscript page with one tab per family, the
 * tab's name, and a grid of recipe entries (result icons). No scrolling or
 * paging; a family that outgrows BOOK_COLUMNS x BOOK_ROWS is cut off.
 *
 * - Known recipes (full contents in {@link AlchemyClientIndex}) use the normal
 *   entry art when the inventory plus the grid can fill them, the "missing" art
 *   otherwise. Hover shows the base and ingredients as icons, then an optional
 *   description from the lang file ({@link #descriptionKey}); click fills the grid.
 * - In SHOW_LOCKED mode every unknown recipe is a locked entry: faded result
 *   icon and name only, drawn from the index. It cannot be clicked.
 * - White Gull (category white_gull) is listed first in the Decoctions tab.
 *
 * The selected tab is remembered for the session. Client only.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class AlchemyRecipeBook {
	/** The tabs, top to bottom. A tab lists every recipe whose category it names, in that order. */
	public enum Tab {
		POTIONS("potions", AlchemyLayout.TAB_ICON_POTIONS_TEX, AlchemyRecipe.Category.POTION),
		OILS("oils", AlchemyLayout.TAB_ICON_OILS_TEX, AlchemyRecipe.Category.OIL),
		BOMBS("bombs", AlchemyLayout.TAB_ICON_BOMBS_TEX, AlchemyRecipe.Category.BOMB),
		DECOCTIONS("decoctions", AlchemyLayout.TAB_ICON_DECOCTIONS_TEX, AlchemyRecipe.Category.WHITE_GULL, AlchemyRecipe.Category.DECOCTION),
		MUTAGENS("mutagens", AlchemyLayout.TAB_ICON_MUTAGENS_TEX, AlchemyRecipe.Category.MUTAGEN);

		private final String key;
		private final String icon;
		private final List<AlchemyRecipe.Category> categories;

		Tab(String key, String icon, AlchemyRecipe.Category... categories) {
			this.key = key;
			this.icon = icon;
			this.categories = List.of(categories);
		}

		public Component title() {
			return Component.translatable("gui.witchercraft.alchemy.tab." + key);
		}
	}

	/** One entry: a known recipe, or a locked one ({@code recipe == null}). */
	private record Entry(Identifier id, AlchemyRecipe.Category category, Item result, AlchemyRecipe recipe) {
		boolean locked() {
			return recipe == null;
		}
	}

	private static final int LOCKED_ICON_FADE = 0x99E6D6AE;
	private static final int HOVER_HIGHLIGHT = 0x40FFFFFF;
	// descriptions wrap to this width in the entry tooltip
	private static final int DESCRIPTION_WIDTH = 160;

	private static Tab selected = Tab.POTIONS;

	private final Font font;
	private final AlchemyMenu menu;
	private final Inventory inventory;
	private int x, y;

	public AlchemyRecipeBook(Font font, AlchemyMenu menu, Inventory inventory) {
		this.font = font;
		this.menu = menu;
		this.inventory = inventory;
	}

	/** Place the page (top-left, screen pixels); the tabs hang off its left edge. */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/** How far the tabs reach left of the page, so the screen can reserve room for them. */
	public static int tabsOut() {
		return Math.max(0, AlchemyLayout.BOOK_TAB_SELECTED_SHIFT - AlchemyLayout.BOOK_TAB_X);
	}

	@SubscribeEvent
	public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(AlchemyRecipeTooltip.class, AlchemyRecipeTooltip.Client::new);
	}

	// ---- entries --------------------------------------------------------------------

	private List<Entry> entries() {
		boolean showLocked = AlchemyClientIndex.mode() == AlchemyKnowledge.Mode.SHOW_LOCKED;
		List<Entry> list = new ArrayList<>();
		for (AlchemyIndexSyncMessage.Entry index : AlchemyClientIndex.recipes()) {
			if (!selected.categories.contains(index.category()))
				continue;
			AlchemyRecipe recipe = AlchemyClientIndex.known(index.id());
			if (recipe != null || showLocked)
				list.add(new Entry(index.id(), index.category(), index.result(), recipe));
		}
		list.sort(Comparator.comparingInt((Entry e) -> selected.categories.indexOf(e.category()))
				.thenComparing(e -> new ItemStack(e.result()).getHoverName().getString()));
		return list;
	}

	private int capacity() {
		return AlchemyLayout.BOOK_COLUMNS * AlchemyLayout.BOOK_ROWS;
	}

	private int entryX(int i) {
		return x + AlchemyLayout.BOOK_GRID_X + (i % AlchemyLayout.BOOK_COLUMNS) * AlchemyLayout.BOOK_ENTRY_SIZE;
	}

	private int entryY(int i) {
		return y + AlchemyLayout.BOOK_GRID_Y + (i / AlchemyLayout.BOOK_COLUMNS) * AlchemyLayout.BOOK_ENTRY_SIZE;
	}

	private Entry entryAt(double mx, double my) {
		List<Entry> entries = entries();
		int size = AlchemyLayout.BOOK_ENTRY_SIZE;
		for (int i = 0; i < entries.size() && i < capacity(); i++)
			if (mx >= entryX(i) && mx < entryX(i) + size && my >= entryY(i) && my < entryY(i) + size)
				return entries.get(i);
		return null;
	}

	// ---- tabs -------------------------------------------------------------------------

	private int tabX(Tab tab) {
		return x + AlchemyLayout.BOOK_TAB_X - (tab == selected ? AlchemyLayout.BOOK_TAB_SELECTED_SHIFT : 0);
	}

	private int tabY(Tab tab) {
		return y + AlchemyLayout.BOOK_TAB_Y + tab.ordinal() * AlchemyLayout.BOOK_TAB_STEP;
	}

	/** The tab under the mouse. Only the part sticking out of the page counts. */
	private Tab tabAt(double mx, double my) {
		for (Tab tab : Tab.values()) {
			int tx = tabX(tab), ty = tabY(tab);
			if (mx >= tx && mx < Math.min(tx + AlchemyLayout.BOOK_TAB_W, x) && my >= ty && my < ty + AlchemyLayout.BOOK_TAB_H)
				return tab;
		}
		return null;
	}

	// ---- input ------------------------------------------------------------------------

	/** Whether a point is on the page or a tab (so a click there never throws the carried item). */
	public boolean contains(double mx, double my) {
		return (mx >= x && mx < x + AlchemyLayout.BOOK_W && my >= y && my < y + AlchemyLayout.BOOK_H) || tabAt(mx, my) != null;
	}

	/** Switch to the tab under the mouse. Returns whether a tab was hit. */
	public boolean clickTab(double mx, double my) {
		Tab tab = tabAt(mx, my);
		if (tab == null)
			return false;
		selected = tab;
		return true;
	}

	/** The known recipe under the mouse, or null (nothing there, or a locked entry). */
	public Identifier recipeAt(double mx, double my) {
		Entry entry = entryAt(mx, my);
		return entry == null || entry.locked() ? null : entry.id();
	}

	// ---- drawing ----------------------------------------------------------------------

	public void extract(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		// tabs first, so the page's edge covers where they tuck under it
		for (Tab tab : Tab.values()) {
			int tx = tabX(tab), ty = tabY(tab);
			blit(g, tab == selected ? AlchemyLayout.BOOK_TAB_SELECTED_TEX : AlchemyLayout.BOOK_TAB_TEX, tx, ty, AlchemyLayout.BOOK_TAB_W, AlchemyLayout.BOOK_TAB_H);
			blit(g, tab.icon, tx + AlchemyLayout.BOOK_TAB_ICON_X, ty + AlchemyLayout.BOOK_TAB_ICON_Y, 16, 16);
		}
		blit(g, AlchemyLayout.BOOK_TEX, x, y, AlchemyLayout.BOOK_W, AlchemyLayout.BOOK_H);

		Component title = selected.title();
		g.text(font, title, x + (AlchemyLayout.BOOK_W - font.width(title)) / 2, y + AlchemyLayout.BOOK_TITLE_Y, AlchemyLayout.BOOK_TITLE_COLOR, false);

		List<ItemStack> pool = availableItems();
		List<Entry> entries = entries();
		int size = AlchemyLayout.BOOK_ENTRY_SIZE;
		for (int i = 0; i < entries.size() && i < capacity(); i++) {
			Entry entry = entries.get(i);
			int ex = entryX(i), ey = entryY(i);
			String art = entry.locked() ? AlchemyLayout.BOOK_ENTRY_LOCKED_TEX
					: canFill(entry.recipe(), pool) ? AlchemyLayout.BOOK_ENTRY_TEX : AlchemyLayout.BOOK_ENTRY_MISSING_TEX;
			blit(g, art, ex, ey, size, size);
			int ix = ex + (size - 16) / 2, iy = ey + (size - 16) / 2;
			g.fakeItem(new ItemStack(entry.result()), ix, iy);
			if (entry.locked())
				g.fill(ix, iy, ix + 16, iy + 16, LOCKED_ICON_FADE);
			else if (mouseX >= ex && mouseX < ex + size && mouseY >= ey && mouseY < ey + size)
				g.fill(ex + 1, ey + 1, ex + size - 1, ey + size - 1, HOVER_HIGHLIGHT);
		}
	}

	/** Tab names, and for entries the result name plus the base and ingredients as icons. */
	public void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		Tab tab = tabAt(mouseX, mouseY);
		if (tab != null) {
			g.setTooltipForNextFrame(font, tab.title(), mouseX, mouseY);
			return;
		}
		Entry entry = entryAt(mouseX, mouseY);
		if (entry == null)
			return;
		Component name = new ItemStack(entry.result()).getHoverName();
		if (entry.locked()) {
			g.setTooltipForNextFrame(font, List.of(name, Component.translatable("gui.witchercraft.alchemy.locked").withStyle(ChatFormatting.GRAY)), Optional.empty(), mouseX, mouseY);
			return;
		}
		// name, then the base and ingredient icons (vanilla puts the image second), then the description
		List<Component> lines = new ArrayList<>();
		lines.add(name);
		String key = descriptionKey(entry.id());
		if (I18n.exists(key))
			for (FormattedText line : font.getSplitter().splitLines(Component.translatable(key), DESCRIPTION_WIDTH, Style.EMPTY))
				lines.add(Component.literal(line.getString()).withStyle(ChatFormatting.GRAY));
		g.setTooltipForNextFrame(font, lines, Optional.of(AlchemyRecipeTooltip.of(entry.recipe())), mouseX, mouseY);
	}

	/**
	 * The lang key of a recipe's optional tooltip description: {@code alchemy.recipe.<namespace>.<path>.description},
	 * with slashes in the path as dots, e.g. {@code alchemy.recipe.witchercraft.alchemy.swallow.description}
	 * for {@code witchercraft:alchemy/swallow}. No entry in the lang file means no description line.
	 */
	public static String descriptionKey(Identifier recipeId) {
		return "alchemy.recipe." + recipeId.getNamespace() + "." + recipeId.getPath().replace('/', '.') + ".description";
	}

	// ---- can the inventory fill it? ----------------------------------------------------

	/** Copies of everything a fill could use: the main inventory, the hotbar, and the base and ingredient slots. */
	private List<ItemStack> availableItems() {
		List<ItemStack> pool = new ArrayList<>();
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++)
			if (!inventory.getItem(slot).isEmpty())
				pool.add(inventory.getItem(slot).copy());
		for (int slot = AlchemyMenu.BASE_SLOT; slot < AlchemyMenu.OUTPUT_SLOT; slot++)
			if (menu.getSlot(slot).hasItem())
				pool.add(menu.getSlot(slot).getItem().copy());
		return pool;
	}

	/** Whether the pool holds the base and every ingredient, one item each (greedy; fine for exact-item and tag recipes alike). */
	private static boolean canFill(AlchemyRecipe recipe, List<ItemStack> available) {
		List<ItemStack> pool = new ArrayList<>(available.size());
		for (ItemStack stack : available)
			pool.add(stack.copy());
		if (!take(pool, recipe.base()))
			return false;
		for (Ingredient ingredient : recipe.ingredients())
			if (!take(pool, ingredient))
				return false;
		return true;
	}

	private static boolean take(List<ItemStack> pool, Ingredient ingredient) {
		for (ItemStack stack : pool) {
			if (!stack.isEmpty() && ingredient.test(stack)) {
				stack.shrink(1);
				return true;
			}
		}
		return false;
	}

	private static void blit(GuiGraphicsExtractor g, String texture, int x, int y, int w, int h) {
		if (texture == null || texture.isEmpty())
			return;
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(texture), x, y, 0, 0, w, h, w, h);
	}
}

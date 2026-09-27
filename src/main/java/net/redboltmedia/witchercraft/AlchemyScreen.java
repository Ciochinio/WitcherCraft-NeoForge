package net.redboltmedia.witchercraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The Alchemy tab. A vanilla container screen for {@link AlchemyMenu} that draws
 * the same shell chrome as {@link WitcherGuiScreen} (letterboxed per-tab
 * background and navbar, through {@link ShellChrome}), so to the player it is
 * just another tab.
 *
 * The chrome scales with the 640x360 design canvas; the slot block does NOT. The
 * circle, output, inventory and recipe book panel stay at normal GUI scale so
 * vanilla handles hover, clicks, shift-click, drag-splitting, double-click
 * collect and the carried item. Any art framing the slots must therefore be
 * drawn with the slot block, never painted into the fullscreen background: the
 * panel, circle ornament and per-slot sprites all come from {@link AlchemyLayout}.
 *
 * Step 0 test build: the recipe book panel is an empty reserved area, and the
 * block and panel are outlined red when they do not fit the content region.
 *
 * HAND-MAINTAINED: locked code element. The screen is bound to the menu type
 * here, not through an MCreator gui element.
 */
public class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
	private static final String TAB_ID = "alchemy";

	private int bookX, bookY;
	private boolean fitsWidth, fitsHeight;
	private boolean swallowRelease;

	public AlchemyScreen(AlchemyMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, AlchemyLayout.BLOCK_W, AlchemyLayout.BLOCK_H);
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class Registration {
		@SubscribeEvent
		public static void registerScreen(RegisterMenuScreensEvent event) {
			event.register(AlchemyMenu.TYPE.get(), AlchemyScreen::new);
		}
	}

	// ---- placement ---------------------------------------------------------------

	@Override
	protected void init() {
		super.init();
		int cx = ShellChrome.contentScreenX(this.width, this.height);
		int cy = ShellChrome.contentScreenY(this.width, this.height);
		int cw = ShellChrome.contentScreenW(this.width, this.height);
		int ch = ShellChrome.contentScreenH(this.width, this.height);

		// the recipe book panel sits left of the block; centre the pair in the content region
		int groupW = AlchemyLayout.BOOK_W + AlchemyLayout.BOOK_GAP + this.imageWidth;
		fitsWidth = groupW <= cw;
		fitsHeight = this.imageHeight <= ch;

		this.leftPos = cx + (cw - groupW) / 2 + AlchemyLayout.BOOK_W + AlchemyLayout.BOOK_GAP + AlchemyLayout.BLOCK_OFFSET_X;
		int top = cy + (ch - this.imageHeight) / 2 + AlchemyLayout.BLOCK_OFFSET_Y;
		// never under the navbar; overflow past the bottom only if it cannot fit at all
		this.topPos = Math.max(cy, Math.min(top, this.height - this.imageHeight));

		this.bookX = this.leftPos - AlchemyLayout.BOOK_GAP - AlchemyLayout.BOOK_W;
		this.bookY = this.topPos;
	}

	// ---- rendering -----------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		ShellChrome.drawBackground(g, this.width, this.height, TAB_ID);
		ShellChrome.pushDesignTransform(g, this.width, this.height);
		ShellChrome.drawNavbar(g, this.font, TAB_ID);
		g.pose().popMatrix();

		// recipe book panel (reserved space for now)
		blit(g, AlchemyLayout.BOOK_TEX, bookX, bookY, AlchemyLayout.BOOK_W, AlchemyLayout.BOOK_H);
		if (!fitsWidth)
			outline(g, bookX, bookY, AlchemyLayout.BOOK_W, AlchemyLayout.BOOK_H, AlchemyLayout.MISFIT_COLOR);

		// slot block: panel, circle ornament, then one sprite per slot
		blit(g, AlchemyLayout.PANEL_TEX, leftPos, topPos, imageWidth, imageHeight);
		blit(g, AlchemyLayout.CIRCLE_TEX, leftPos + AlchemyLayout.CIRCLE_X, topPos + AlchemyLayout.CIRCLE_Y, AlchemyLayout.CIRCLE_W, AlchemyLayout.CIRCLE_H);
		if (!fitsHeight)
			outline(g, leftPos, topPos, imageWidth, imageHeight, AlchemyLayout.MISFIT_COLOR);

		int size = AlchemyLayout.SLOT_TEX_SIZE;
		for (Slot slot : this.menu.slots)
			blit(g, slotTexture(slot.index), leftPos + slot.x - 1, topPos + slot.y - 1, size, size);
	}

	private static String slotTexture(int index) {
		if (index == AlchemyMenu.BASE_SLOT)
			return AlchemyLayout.SLOT_BASE_TEX;
		if (index < AlchemyMenu.OUTPUT_SLOT)
			return AlchemyLayout.SLOT_INGREDIENT_TEX;
		if (index == AlchemyMenu.OUTPUT_SLOT)
			return AlchemyLayout.SLOT_OUTPUT_TEX;
		return AlchemyLayout.SLOT_INVENTORY_TEX;
	}

	/** Draw a whole texture stretched to a rect; an empty path draws nothing. */
	private static void blit(GuiGraphicsExtractor g, String texture, int x, int y, int w, int h) {
		if (texture == null || texture.isEmpty())
			return;
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(texture), x, y, 0, 0, w, h, w, h);
	}

	private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x, y, x + w, y + 1, color);
		g.fill(x, y + h - 1, x + w, y + h, color);
		g.fill(x, y, x + 1, y + h, color);
		g.fill(x + w - 1, y, x + w, y + h, color);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		// no vanilla title/inventory labels: the circle occupies the title's spot
	}

	// ---- input -----------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			float s = ShellChrome.layoutScale(this.width, this.height);
			int dmx = ShellChrome.toDesignX(event.x(), ShellChrome.offsetX(this.width, s), s);
			int dmy = ShellChrome.toDesignY(event.y(), ShellChrome.offsetY(this.height, s), s);
			String pid = ShellChrome.navTabAt(dmx, dmy);
			if (pid != null) {
				swallowRelease = true;
				if (!pid.equals(TAB_ID))
					switchToTab(pid);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		// the release of a navbar click must not reach the container logic (it
		// would treat it as a click outside the block and throw the carried item)
		if (swallowRelease) {
			swallowRelease = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
		// the recipe book panel counts as inside, so a click there never throws the carried item
		if (mx >= bookX && mx < bookX + AlchemyLayout.BOOK_W && my >= bookY && my < bookY + AlchemyLayout.BOOK_H)
			return false;
		return super.hasClickedOutside(mx, my, xo, yo);
	}

	/**
	 * Close the menu on the server (it returns the grid) and show the shell on
	 * another tab, without dropping to the world in between: a
	 * {@code closeContainer()} would clear the screen first, re-grabbing and
	 * re-centring the cursor.
	 */
	private void switchToTab(String pageId) {
		this.minecraft.getConnection().send(new ServerboundContainerClosePacket(this.menu.containerId));
		this.minecraft.player.containerMenu = this.minecraft.player.inventoryMenu;
		if (WitcherGuiPages.isContainerTab(pageId))
			WitcherGuiPages.open(pageId); // another container tab: the server replaces this screen
		else
			this.minecraft.setScreen(new WitcherGuiScreen(pageId));
	}
}

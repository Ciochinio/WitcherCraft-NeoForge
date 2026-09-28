package net.redboltmedia.witchercraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

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
 * panel, frame, rings, lines and per-slot sprites all come from
 * {@link AlchemyLayout}.
 *
 * The book button shows or hides the recipe book panel, like vanilla's recipe
 * book: shown, the book and block are centred as a pair; hidden, the block is
 * centred alone. The choice is remembered in the client config. The panel is
 * empty until slice 3. The block and panel are outlined red when they do not fit
 * the content region.
 *
 * HAND-MAINTAINED: locked code element. The screen is bound to the menu type
 * here, not through an MCreator gui element.
 */
public class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
	private static final String TAB_ID = "alchemy";

	private boolean bookOpen;
	private int bookX, bookY;
	private boolean fitsWidth, fitsHeight;
	private boolean swallowRelease;

	public AlchemyScreen(AlchemyMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, AlchemyLayout.BLOCK_W, AlchemyLayout.BLOCK_H);
		this.bookOpen = WorldMapClientConfig.alchemyRecipeBookOpen();
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
		placeBlock();
	}

	/** Centre the block (with the book panel to its left, if shown) in the shell's content region. */
	private void placeBlock() {
		int cx = ShellChrome.contentScreenX(this.width, this.height);
		int cy = ShellChrome.contentScreenY(this.width, this.height);
		int cw = ShellChrome.contentScreenW(this.width, this.height);
		int ch = ShellChrome.contentScreenH(this.width, this.height);

		int bookSpan = bookOpen ? AlchemyLayout.BOOK_W + AlchemyLayout.BOOK_GAP : 0;
		int groupW = bookSpan + this.imageWidth;
		fitsWidth = groupW <= cw;
		fitsHeight = this.imageHeight <= ch;

		this.leftPos = cx + (cw - groupW) / 2 + bookSpan + AlchemyLayout.BLOCK_OFFSET_X;
		int top = cy + (ch - this.imageHeight) / 2 + AlchemyLayout.BLOCK_OFFSET_Y;
		// never under the navbar; overflow past the bottom only if it cannot fit at all
		this.topPos = Math.max(cy, Math.min(top, this.height - this.imageHeight));

		this.bookX = this.leftPos - AlchemyLayout.BOOK_GAP - AlchemyLayout.BOOK_W;
		this.bookY = this.topPos;
	}

	private void toggleBook() {
		bookOpen = !bookOpen;
		WorldMapClientConfig.setAlchemyRecipeBookOpen(bookOpen);
		placeBlock();
	}

	// ---- rendering -----------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		ShellChrome.drawBackground(g, this.width, this.height, TAB_ID);
		ShellChrome.pushDesignTransform(g, this.width, this.height);
		ShellChrome.drawNavbar(g, this.font, TAB_ID);
		g.pose().popMatrix();

		// recipe book panel (reserved space until slice 3)
		if (bookOpen) {
			blit(g, AlchemyLayout.BOOK_TEX, bookX, bookY, AlchemyLayout.BOOK_W, AlchemyLayout.BOOK_H);
			if (!fitsWidth)
				outline(g, bookX, bookY, AlchemyLayout.BOOK_W, AlchemyLayout.BOOK_H, AlchemyLayout.MISFIT_COLOR);
		}

		// slot block: panel, optional frame image, lines, rings, then one sprite per slot
		blit(g, AlchemyLayout.PANEL_TEX, leftPos, topPos, imageWidth, imageHeight);
		blit(g, AlchemyLayout.FRAME_TEX, leftPos + AlchemyLayout.FRAME_TEX_X, topPos + AlchemyLayout.FRAME_TEX_Y, AlchemyLayout.FRAME_TEX_W, AlchemyLayout.FRAME_TEX_H);
		if (!fitsHeight || (!bookOpen && !fitsWidth))
			outline(g, leftPos, topPos, imageWidth, imageHeight, AlchemyLayout.MISFIT_COLOR);

		drawCircleLines(g);
		ring(g, AlchemyLayout.RING_BASE_TEX, AlchemyLayout.BASE_X, AlchemyLayout.BASE_Y, AlchemyLayout.RING_BASE_SIZE);
		for (int i = 0; i < AlchemyMenu.INGREDIENT_SLOTS; i++)
			ring(g, AlchemyLayout.RING_INGREDIENT_TEX, AlchemyLayout.INGREDIENT_X[i], AlchemyLayout.INGREDIENT_Y[i], AlchemyLayout.RING_INGREDIENT_SIZE);
		ring(g, AlchemyLayout.RING_OUTPUT_TEX, AlchemyLayout.OUTPUT_X, AlchemyLayout.OUTPUT_Y, AlchemyLayout.RING_OUTPUT_SIZE);

		int size = AlchemyLayout.SLOT_TEX_SIZE;
		for (Slot slot : this.menu.slots)
			blit(g, slotTexture(slot.index), leftPos + slot.x - 1, topPos + slot.y - 1, size, size);

		// Brew button: art, then a centred label
		boolean hover = overBrew(mouseX, mouseY);
		int bx = leftPos + AlchemyLayout.BREW_X, by = topPos + AlchemyLayout.BREW_Y;
		blit(g, hover ? AlchemyLayout.BREW_HOVER_TEX : AlchemyLayout.BREW_TEX, bx, by, AlchemyLayout.BREW_W, AlchemyLayout.BREW_H);
		Component label = Component.translatable("gui.witchercraft.alchemy.brew");
		g.text(this.font, label, bx + (AlchemyLayout.BREW_W - this.font.width(label)) / 2, by + (AlchemyLayout.BREW_H - 8) / 2,
				hover ? AlchemyLayout.BREW_TEXT_HOVER_COLOR : AlchemyLayout.BREW_TEXT_COLOR, false);

		// recipe book show/hide button (vanilla's 20x18), with the book icon centred on it
		int kx = leftPos + AlchemyLayout.BOOK_BUTTON_X, ky = topPos + AlchemyLayout.BOOK_BUTTON_Y;
		blit(g, overBookButton(mouseX, mouseY) ? AlchemyLayout.BOOK_BUTTON_HOVER_TEX : AlchemyLayout.BOOK_BUTTON_TEX,
				kx, ky, AlchemyLayout.BOOK_BUTTON_W, AlchemyLayout.BOOK_BUTTON_H);
		blit(g, AlchemyLayout.BOOK_ICON_TEX, kx + (AlchemyLayout.BOOK_BUTTON_W - 16) / 2, ky + (AlchemyLayout.BOOK_BUTTON_H - 16) / 2, 16, 16);

		drawFeedback(g);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		super.extractRenderState(g, mouseX, mouseY, partial);
		if (overBookButton(mouseX, mouseY))
			g.setTooltipForNextFrame(this.font, Component.translatable(bookOpen ? "gui.witchercraft.alchemy.hide_recipes" : "gui.witchercraft.alchemy.show_recipes"), mouseX, mouseY);
	}

	/** A ring texture centred on a slot's 16x16 item area. */
	private void ring(GuiGraphicsExtractor g, String texture, int slotX, int slotY, int size) {
		blit(g, texture, leftPos + slotX + 8 - size / 2, topPos + slotY + 8 - size / 2, size, size);
	}

	/** The circle's lines as horizontal pixel runs {x, y, length, colour} in block coords; built once. */
	private static int[][] circleLineRuns;

	private void drawCircleLines(GuiGraphicsExtractor g) {
		if (circleLineRuns == null)
			circleLineRuns = buildCircleLineRuns();
		for (int[] run : circleLineRuns)
			g.fill(leftPos + run[0], topPos + run[1], leftPos + run[0] + run[2], topPos + run[1] + 1, run[3]);
	}

	/**
	 * The frame (a pentagon through the ingredient slots) and the spokes (base to each
	 * ingredient), between slot centres, so they follow the slots wherever the layout
	 * puts them. Hard 1x pixel art: every GUI pixel within width / 2 of a line gets its
	 * colour, and within width / 2 + LINE_OUTLINE the outline colour. Outlines are
	 * painted first, then spokes, then the frame, so outlines never cut across a line.
	 * Each line stops LINE_GAP outside the rings (cut-corner squares, RING_CORNER), so
	 * it ends cleanly instead of being clipped by a ring edge at a different angle.
	 * Slot centres sit on pixel boundaries, so an even width is sampled at pixel
	 * centres and an odd width half a pixel over; otherwise a straight line would
	 * split into half-covered pixels. The layout tool draws the same pixels.
	 */
	private static int[][] buildCircleLineRuns() {
		int n = AlchemyMenu.INGREDIENT_SLOTS;
		float bx = AlchemyLayout.BASE_X + 8f, by = AlchemyLayout.BASE_Y + 8f;
		float ringBase = AlchemyLayout.RING_BASE_SIZE / 2f, ringIng = AlchemyLayout.RING_INGREDIENT_SIZE / 2f;
		List<float[]> frame = new ArrayList<>(), spokes = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			float ix = AlchemyLayout.INGREDIENT_X[i] + 8f, iy = AlchemyLayout.INGREDIENT_Y[i] + 8f;
			int j = (i + 1) % n;
			float jx = AlchemyLayout.INGREDIENT_X[j] + 8f, jy = AlchemyLayout.INGREDIENT_Y[j] + 8f;
			float[] edge = trimmedLine(ix, iy, ringIng, jx, jy, ringIng);
			if (edge != null)
				frame.add(edge);
			float[] spoke = trimmedLine(bx, by, ringBase, ix, iy, ringIng);
			if (spoke != null)
				spokes.add(spoke);
		}
		float frameW = AlchemyLayout.FRAME_LINE_WIDTH, spokeW = AlchemyLayout.SPOKE_WIDTH;
		int outline = Math.max(0, AlchemyLayout.LINE_OUTLINE);

		// paint into a grid over the block's area plus a margin for lines past its edge
		int margin = (int) Math.ceil(Math.max(frameW, spokeW) / 2f) + outline + 2;
		int gx = -margin, gy = -margin, gw = AlchemyLayout.BLOCK_W + 2 * margin, gh = AlchemyLayout.BLOCK_H + 2 * margin;
		int[] grid = new int[gw * gh];
		if (outline > 0) {
			if (frameW > 0f)
				paintLines(grid, gx, gy, gw, gh, frame, frameW, outline, AlchemyLayout.LINE_OUTLINE_COLOR);
			if (spokeW > 0f)
				paintLines(grid, gx, gy, gw, gh, spokes, spokeW, outline, AlchemyLayout.LINE_OUTLINE_COLOR);
		}
		if (spokeW > 0f)
			paintLines(grid, gx, gy, gw, gh, spokes, spokeW, 0, AlchemyLayout.SPOKE_COLOR);
		if (frameW > 0f)
			paintLines(grid, gx, gy, gw, gh, frame, frameW, 0, AlchemyLayout.FRAME_LINE_COLOR);

		List<int[]> runs = new ArrayList<>();
		for (int y = 0; y < gh; y++) {
			int x = 0;
			while (x < gw) {
				int color = grid[y * gw + x];
				int start = x;
				while (x < gw && grid[y * gw + x] == color)
					x++;
				if (color != 0)
					runs.add(new int[]{gx + start, gy + y, x - start, color});
			}
		}
		return runs.toArray(new int[0][]);
	}

	/**
	 * The line from ring A to ring B, trimmed to start and end LINE_GAP outside both
	 * rings, as {x1, y1, x2, y2}; null if the rings leave no room for it.
	 */
	private static float[] trimmedLine(float ax, float ay, float ringA, float bx, float by, float ringB) {
		float dx = bx - ax, dy = by - ay;
		float length = (float) Math.sqrt(dx * dx + dy * dy);
		if (length <= 0f)
			return null;
		float ux = dx / length, uy = dy / length;
		float from = ringExit(ux, uy, ringA) + AlchemyLayout.LINE_GAP;
		float to = length - ringExit(ux, uy, ringB) - AlchemyLayout.LINE_GAP;
		if (to <= from)
			return null;
		return new float[]{ax + ux * from, ay + uy * from, ax + ux * to, ay + uy * to};
	}

	/** Distance from a ring's centre, along a unit direction, to its outer edge (a square of half-size h with RING_CORNER cut corners). */
	private static float ringExit(float ux, float uy, float h) {
		float ax = Math.abs(ux), ay = Math.abs(uy);
		float t = (2f * h - AlchemyLayout.RING_CORNER) / (ax + ay);
		if (ax > 0f)
			t = Math.min(t, h / ax);
		if (ay > 0f)
			t = Math.min(t, h / ay);
		return t;
	}

	/** Fill every grid pixel within width / 2 + extra of any of the lines (round ends). */
	private static void paintLines(int[] grid, int gx, int gy, int gw, int gh, List<float[]> lines, float width, int extra, int color) {
		float reach = width / 2f + extra;
		float offset = Math.round(width) % 2 == 1 ? 0f : 0.5f;
		for (float[] l : lines) {
			int minX = (int) Math.floor(Math.min(l[0], l[2]) - reach) - 1, maxX = (int) Math.ceil(Math.max(l[0], l[2]) + reach) + 1;
			int minY = (int) Math.floor(Math.min(l[1], l[3]) - reach) - 1, maxY = (int) Math.ceil(Math.max(l[1], l[3]) + reach) + 1;
			for (int py = Math.max(minY, gy); py <= Math.min(maxY, gy + gh - 1); py++) {
				for (int px = Math.max(minX, gx); px <= Math.min(maxX, gx + gw - 1); px++) {
					if (distanceToLine(px + offset, py + offset, l) <= reach)
						grid[(py - gy) * gw + (px - gx)] = color;
				}
			}
		}
	}

	private static float distanceToLine(float px, float py, float[] l) {
		float vx = l[2] - l[0], vy = l[3] - l[1];
		float len2 = vx * vx + vy * vy;
		float t = len2 <= 0f ? 0f : Math.max(0f, Math.min(1f, ((px - l[0]) * vx + (py - l[1]) * vy) / len2));
		float qx = l[0] + t * vx - px, qy = l[1] + t * vy - py;
		return (float) Math.sqrt(qx * qx + qy * qy);
	}

	// ---- brew feedback ---------------------------------------------------------------

	private Component feedback;
	private long feedbackShownAt;

	/** Show a short message below the Brew button (sent by the server after a Brew click). */
	public void showFeedback(Component text) {
		this.feedback = text;
		this.feedbackShownAt = Util.getMillis();
	}

	private void drawFeedback(GuiGraphicsExtractor g) {
		if (feedback == null)
			return;
		long age = Util.getMillis() - feedbackShownAt;
		if (age >= AlchemyLayout.MESSAGE_MS) {
			feedback = null;
			return;
		}
		long left = AlchemyLayout.MESSAGE_MS - age;
		float alpha = Math.min(1f, (float) left / AlchemyLayout.MESSAGE_FADE_MS);
		int a = Math.max(8, (int) (alpha * 255f)); // text below ~4 alpha is skipped entirely
		int color = (a << 24) | AlchemyLayout.MESSAGE_COLOR;
		int y = topPos + AlchemyLayout.MESSAGE_Y;
		for (FormattedCharSequence line : this.font.split(feedback, AlchemyLayout.MESSAGE_W)) {
			g.text(this.font, line, leftPos + AlchemyLayout.MESSAGE_CENTER_X - this.font.width(line) / 2, y, color, false);
			y += this.font.lineHeight;
		}
	}

	private boolean overBrew(double mx, double my) {
		int bx = leftPos + AlchemyLayout.BREW_X, by = topPos + AlchemyLayout.BREW_Y;
		return mx >= bx && mx < bx + AlchemyLayout.BREW_W && my >= by && my < by + AlchemyLayout.BREW_H;
	}

	private boolean overBookButton(double mx, double my) {
		int bx = leftPos + AlchemyLayout.BOOK_BUTTON_X, by = topPos + AlchemyLayout.BOOK_BUTTON_Y;
		return mx >= bx && mx < bx + AlchemyLayout.BOOK_BUTTON_W && my >= by && my < by + AlchemyLayout.BOOK_BUTTON_H;
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
			if (overBrew(event.x(), event.y())) {
				swallowRelease = true;
				playClick();
				ClientPacketDistributor.sendToServer(AlchemyBrewMessage.INSTANCE);
				return true;
			}
			if (overBookButton(event.x(), event.y())) {
				swallowRelease = true;
				playClick();
				toggleBook();
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void playClick() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		// the release of a button or navbar click must not reach the container logic
		// (it would treat it as a click outside the block and throw the carried item)
		if (swallowRelease) {
			swallowRelease = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
		// the shown recipe book panel counts as inside, so a click there never throws the carried item
		if (bookOpen && mx >= bookX && mx < bookX + AlchemyLayout.BOOK_W && my >= bookY && my < bookY + AlchemyLayout.BOOK_H)
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

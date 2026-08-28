package net.redboltmedia.witchercraft.client.gui.shell;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * The GUI shell - a persistent, client-only Screen that owns the navbar + frame
 * and swaps which {@link GuiPage} fills its content region based on the
 * {@code activeTabId} state. Think {@code <App>}: the shell stays mounted;
 * clicking a tab is {@code setState(activeTabId)} and re-renders. No container,
 * no server menu - opened directly with {@code Minecraft.setScreen}.
 *
 * HAND-MAINTAINED: this class has no MCreator element and is never regenerated.
 * Navbar visuals/order come from {@link WitcherGuiLayout#NAV} (tool-edited);
 * page behaviour comes from {@link WitcherGuiPages}.
 */
public class WitcherGuiScreen extends Screen {

	// Palette (shared with the tools' dark theme).
	private static final int SCRIM = 0xC0000000;
	private static final int PANEL_BG = 0xF00E0E12;
	private static final int PANEL_BORDER = 0xFF2A2A30;
	private static final int NAV_BG = 0xFF15151B;
	private static final int NAV_DIVIDER = 0xFF33333D;
	private static final int TAB_TEXT = 0xFF9A9AA2;
	private static final int TAB_TEXT_ACTIVE = 0xFFFFFFFF;
	private static final int TAB_ACCENT = 0xFFFFDD55;

	private int leftPos, topPos;
	private String activeTabId;

	public WitcherGuiScreen() {
		super(Component.translatable("gui.witchercraft.shell.title"));
		this.activeTabId = WitcherGuiPages.defaultPageId();
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos = WitcherGuiLayout.leftPos(this.width);
		this.topPos = WitcherGuiLayout.topPos(this.height);
		activePage().onShown();
	}

	private GuiPage activePage() {
		return WitcherGuiPages.forId(activeTabId);
	}

	private int contentOriginX() {
		return leftPos + WitcherGuiLayout.CONTENT_X;
	}

	private int contentOriginY() {
		return topPos + WitcherGuiLayout.CONTENT_Y;
	}

	// ---- rendering -----------------------------------------------------------
	// This generator drives screens through extractBackground + extractRenderState
	// (both take GuiGraphicsExtractor), not a render(GuiGraphics) override.

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		super.extractBackground(g, mouseX, mouseY, partial); // vanilla dim / blur
		g.fill(0, 0, this.width, this.height, SCRIM);

		int px = leftPos, py = topPos;
		int pw = WitcherGuiLayout.PANEL_W, ph = WitcherGuiLayout.PANEL_H;
		// panel
		g.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, PANEL_BORDER);
		g.fill(px, py, px + pw, py + ph, PANEL_BG);
		// navbar band
		g.fill(px, py, px + pw, py + WitcherGuiLayout.NAV_Y + WitcherGuiLayout.NAV_H + 2, NAV_BG);
		g.fill(px, py + WitcherGuiLayout.NAV_Y + WitcherGuiLayout.NAV_H + 2, px + pw, py + WitcherGuiLayout.NAV_Y + WitcherGuiLayout.NAV_H + 3, NAV_DIVIDER);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		super.extractRenderState(g, mouseX, mouseY, partial); // widgets, if any
		drawNavbar(g);
		// active page fills the content region (also queues its tooltips)
		activePage().render(g, contentOriginX(), contentOriginY(), mouseX, mouseY, partial);
	}

	private void drawNavbar(GuiGraphicsExtractor g) {
		Font font = this.font;
		for (WitcherGuiLayout.Nav nav : WitcherGuiLayout.NAV) {
			boolean active = nav.pageId.equals(activeTabId);
			Component label = navLabel(nav);
			int tx = leftPos + nav.x, ty = topPos + nav.y;
			int tw = font.width(label);
			g.text(font, label, tx + (nav.w - tw) / 2, ty + (nav.h - 8) / 2, active ? TAB_TEXT_ACTIVE : TAB_TEXT, false);
			if (active)
				g.fill(tx + 2, ty + nav.h, tx + nav.w - 2, ty + nav.h + 1, TAB_ACCENT);
		}
	}

	private Component navLabel(WitcherGuiLayout.Nav nav) {
		if (nav.labelKey != null && !nav.labelKey.isEmpty())
			return Component.translatable(nav.labelKey);
		return WitcherGuiPages.forId(nav.pageId).navLabel();
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			int mx = (int) event.x(), my = (int) event.y();
			for (WitcherGuiLayout.Nav nav : WitcherGuiLayout.NAV) {
				int tx = leftPos + nav.x, ty = topPos + nav.y;
				if (mx >= tx && mx < tx + nav.w && my >= ty && my < ty + nav.h) {
					if (!nav.pageId.equals(activeTabId)) {
						activeTabId = nav.pageId;
						activePage().onShown();
					}
					return true;
				}
			}
		}
		if (activePage().mouseClicked(contentOriginX(), contentOriginY(), event.x(), event.y(), event.button()))
			return true;
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (activePage().keyPressed(key))
			return true;
		return super.keyPressed(event); // Esc closes via onClose()
	}

	@Override
	public void onClose() {
		activePage().onClose();
		this.minecraft.setScreen(null); // opened via setScreen, so close the same way
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

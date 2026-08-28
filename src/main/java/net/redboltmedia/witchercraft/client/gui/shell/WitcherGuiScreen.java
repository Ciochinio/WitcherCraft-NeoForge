package net.redboltmedia.witchercraft.client.gui.shell;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * The GUI shell - a persistent, client-only, FULLSCREEN Screen that fills the
 * screen with a background image and draws a navbar + the active {@link GuiPage}
 * on top of it. Think {@code <App>}: the shell stays mounted; clicking a tab is
 * {@code setState(activeTabId)} and re-renders. No container, no server menu -
 * opened directly with {@code Minecraft.setScreen}, optionally onto a chosen tab
 * (used by the per-page keybinds).
 *
 * HAND-MAINTAINED: this class has no MCreator element and is never regenerated.
 * Navbar contents/order come from {@link WitcherGuiLayout#NAV} (tool-edited);
 * page behaviour comes from {@link WitcherGuiPages}; tab positions are computed
 * here (centred group) so the layout is resolution-independent.
 */
public class WitcherGuiScreen extends Screen {

	private static final int SCREEN_DIM = 0x88000000; // slight darken over the bg for contrast
	private static final int CONTENT_SCRIM = 0x66000000; // panel behind the content safe area
	private static final int TAB_BG = 0x66101015;
	private static final int TAB_BG_ACTIVE = 0xB0000000;
	private static final int TAB_BORDER = 0xFF33333D;
	private static final int TAB_TEXT = 0xFFC9C9D2;
	private static final int TAB_TEXT_ACTIVE = 0xFFFFFFFF;
	private static final int TAB_ACCENT = 0xFFFFDD55;

	private String activeTabId;

	public WitcherGuiScreen() {
		this(WitcherGuiPages.defaultPageId());
	}

	/** Open directly on a specific tab (falls back to the default if unknown). */
	public WitcherGuiScreen(String pageId) {
		super(Component.translatable("gui.witchercraft.shell.title"));
		this.activeTabId = isKnownTab(pageId) ? pageId : WitcherGuiPages.defaultPageId();
	}

	private static boolean isKnownTab(String pageId) {
		if (pageId == null)
			return false;
		for (WitcherGuiLayout.Nav n : WitcherGuiLayout.NAV)
			if (n.pageId.equals(pageId))
				return true;
		return false;
	}

	@Override
	protected void init() {
		super.init();
		activePage().onShown();
	}

	private GuiPage activePage() {
		return WitcherGuiPages.forId(activeTabId);
	}

	private int contentOriginX() {
		return WitcherGuiLayout.contentX(this.width);
	}

	private int contentOriginY() {
		return WitcherGuiLayout.contentY(this.height);
	}

	// ---- rendering -----------------------------------------------------------
	// This generator drives screens through extractBackground + extractRenderState
	// (both take GuiGraphicsExtractor), not a render(GuiGraphics) override.

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		// fullscreen background image, stretched to fill (u=0,v=0, texW/H = screen
		// size makes the sampler span the whole texture across the screen).
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(WitcherGuiLayout.BG), 0, 0, 0, 0, this.width, this.height, this.width, this.height);
		g.fill(0, 0, this.width, this.height, SCREEN_DIM);

		// scrim behind the content safe area for readability over busy art
		int cx = contentOriginX(), cy = contentOriginY();
		g.fill(cx - 6, cy - 6, cx + WitcherGuiLayout.CONTENT_W + 6, cy + WitcherGuiLayout.CONTENT_H + 6, CONTENT_SCRIM);
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
		int count = WitcherGuiLayout.NAV.length;
		for (int i = 0; i < count; i++) {
			WitcherGuiLayout.Nav nav = WitcherGuiLayout.NAV[i];
			boolean active = nav.pageId.equals(activeTabId);
			int tx = WitcherGuiLayout.navTabX(this.width, count, i);
			int ty = WitcherGuiLayout.NAV_Y;
			int tw = WitcherGuiLayout.NAV_TAB_W, th = WitcherGuiLayout.NAV_H;

			// tab background + border
			g.fill(tx, ty, tx + tw, ty + th, active ? TAB_BG_ACTIVE : TAB_BG);
			g.fill(tx, ty, tx + tw, ty + 1, TAB_BORDER);
			g.fill(tx, ty + th - 1, tx + tw, ty + th, TAB_BORDER);

			// icon (centred near the top of the tab)
			if (nav.icon != null && !nav.icon.isEmpty()) {
				int ix = tx + (tw - WitcherGuiLayout.NAV_ICON) / 2;
				g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(nav.icon), ix, ty + 2, 0, 0, WitcherGuiLayout.NAV_ICON, WitcherGuiLayout.NAV_ICON, WitcherGuiLayout.NAV_ICON, WitcherGuiLayout.NAV_ICON);
			}

			// label under the icon
			Component label = navLabel(nav);
			int lw = font.width(label);
			g.text(font, label, tx + (tw - lw) / 2, ty + th - 10, active ? TAB_TEXT_ACTIVE : TAB_TEXT, false);

			// active underline
			if (active)
				g.fill(tx + 3, ty + th, tx + tw - 3, ty + th + 1, TAB_ACCENT);
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
			int count = WitcherGuiLayout.NAV.length;
			for (int i = 0; i < count; i++) {
				int tx = WitcherGuiLayout.navTabX(this.width, count, i);
				int ty = WitcherGuiLayout.NAV_Y;
				if (mx >= tx && mx < tx + WitcherGuiLayout.NAV_TAB_W && my >= ty && my < ty + WitcherGuiLayout.NAV_H) {
					String pid = WitcherGuiLayout.NAV[i].pageId;
					if (!pid.equals(activeTabId)) {
						activeTabId = pid;
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

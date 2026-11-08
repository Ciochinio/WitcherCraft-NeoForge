package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The GUI shell's CHROME, shared by every screen that presents itself as a shell
 * tab: the design-canvas transform, the letterboxed per-tab background, and the
 * navbar (with the level readout). {@link WitchercraftGuiScreen} draws its client-only
 * pages with it; container-backed tabs such as {@link AlchemyScreen} draw the same
 * chrome around real item slots, so to the player every tab looks alike.
 *
 * All navbar geometry is DESIGN-canvas pixels ({@link WitchercraftGuiLayout}); callers
 * pass the real (gui-scaled) screen size and this class maps between the two.
 *
 * HAND-MAINTAINED: locked code element, never regenerated.
 */
public final class ShellChrome {
	private ShellChrome() {
	}

	private static final int LETTERBOX = 0xFF000000; // opaque black behind everything
	private static final int PANEL_DIM = 0x33000000; // gentle darken over the bg image
	private static final int TAB_BG = 0x66101015;
	private static final int TAB_BG_ACTIVE = 0xB0000000;
	private static final int TAB_BORDER = 0xFF33333D;
	private static final int TAB_TEXT = 0xFFC9C9D2;
	private static final int TAB_TEXT_ACTIVE = 0xFFFFFFFF;
	private static final int TAB_ACCENT = 0xFFFFDD55;

	// ---- design-canvas <-> screen transform ----------------------------------

	/** Uniform scale to fit the 16:9 design canvas inside the (gui-scaled) screen. */
	public static float layoutScale(int width, int height) {
		return Math.min((float) width / WitchercraftGuiLayout.DESIGN_W, (float) height / WitchercraftGuiLayout.DESIGN_H);
	}

	public static float offsetX(int width, float s) {
		return (width - WitchercraftGuiLayout.DESIGN_W * s) / 2f;
	}

	public static float offsetY(int height, float s) {
		return (height - WitchercraftGuiLayout.DESIGN_H * s) / 2f;
	}

	public static int toDesignX(double sx, float ox, float s) {
		return (int) ((sx - ox) / s);
	}

	public static int toDesignY(double sy, float oy, float s) {
		return (int) ((sy - oy) / s);
	}

	/** Push the design->screen transform. Pair with {@code g.pose().popMatrix()}. */
	public static void pushDesignTransform(GuiGraphicsExtractor g, int width, int height) {
		float s = layoutScale(width, height);
		g.pose().pushMatrix();
		g.pose().translate(offsetX(width, s), offsetY(height, s));
		g.pose().scale(s, s);
	}

	// ---- background ------------------------------------------------------------

	/**
	 * Opaque black over the whole screen (the letterbox bars on non-16:9 screens),
	 * then the tab's background image filling the 16:9 design canvas.
	 */
	public static void drawBackground(GuiGraphicsExtractor g, int width, int height, String tabId) {
		g.fill(0, 0, width, height, LETTERBOX);
		pushDesignTransform(g, width, height);
		String bg = WitchercraftGuiLayout.backgroundFor(tabId);
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(bg), 0, 0, 0, 0, WitchercraftGuiLayout.DESIGN_W, WitchercraftGuiLayout.DESIGN_H, WitchercraftGuiLayout.DESIGN_W, WitchercraftGuiLayout.DESIGN_H);
		g.fill(0, 0, WitchercraftGuiLayout.DESIGN_W, WitchercraftGuiLayout.DESIGN_H, PANEL_DIM);
		g.pose().popMatrix();
	}

	// ---- navbar ------------------------------------------------------------------

	/** Draw the navbar and level readout. Design coords: call inside the design transform. */
	public static void drawNavbar(GuiGraphicsExtractor g, Font font, String activeTabId) {
		drawLevelReadout(g, font);
		int count = WitchercraftGuiLayout.NAV.length;
		for (int i = 0; i < count; i++) {
			WitchercraftGuiLayout.Nav nav = WitchercraftGuiLayout.NAV[i];
			boolean active = nav.pageId.equals(activeTabId);
			int tx = WitchercraftGuiLayout.navTabX(count, i);
			int ty = WitchercraftGuiLayout.NAV_Y;
			int tw = WitchercraftGuiLayout.NAV_TAB_W, th = WitchercraftGuiLayout.NAV_H;

			g.fill(tx, ty, tx + tw, ty + th, active ? TAB_BG_ACTIVE : TAB_BG);
			g.fill(tx, ty, tx + tw, ty + 1, TAB_BORDER);
			g.fill(tx, ty + th - 1, tx + tw, ty + th, TAB_BORDER);

			if (nav.icon != null && !nav.icon.isEmpty()) {
				int ix = tx + (tw - WitchercraftGuiLayout.NAV_ICON) / 2;
				g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(nav.icon), ix, ty + 2, 0, 0, WitchercraftGuiLayout.NAV_ICON, WitchercraftGuiLayout.NAV_ICON, WitchercraftGuiLayout.NAV_ICON, WitchercraftGuiLayout.NAV_ICON);
			}

			Component label = navLabel(nav);
			int lw = font.width(label);
			g.text(font, label, tx + (tw - lw) / 2, ty + th - 10, active ? TAB_TEXT_ACTIVE : TAB_TEXT, false);

			if (active)
				g.fill(tx + 3, ty + th, tx + tw - 3, ty + th + 1, TAB_ACCENT);
		}
	}

	private static Component navLabel(WitchercraftGuiLayout.Nav nav) {
		if (nav.labelKey != null && !nav.labelKey.isEmpty())
			return Component.translatable(nav.labelKey);
		return WitchercraftGuiPages.forId(nav.pageId).navLabel();
	}

	/** The pageId of the navbar tab under a DESIGN-coords point, or null. */
	public static String navTabAt(int dmx, int dmy) {
		int count = WitchercraftGuiLayout.NAV.length;
		for (int i = 0; i < count; i++) {
			int tx = WitchercraftGuiLayout.navTabX(count, i);
			int ty = WitchercraftGuiLayout.NAV_Y;
			if (dmx >= tx && dmx < tx + WitchercraftGuiLayout.NAV_TAB_W && dmy >= ty && dmy < ty + WitchercraftGuiLayout.NAV_H)
				return WitchercraftGuiLayout.NAV[i].pageId;
		}
		return null;
	}

	// ---- compact navbar (SCREEN coords) -------------------------------------------
	// A small icon bar for screens where item-list mods (JEI and similar) own the
	// side strips: the decorated vanilla inventory draws it directly above its
	// panel, the one area those mods leave alone. Level badge on the left, then one
	// icon tab per NAV entry; labels show as tooltips.

	public static final int COMPACT_H = 20;
	public static final int COMPACT_GAP = 2;
	public static final int COMPACT_LEVEL_W = 24;
	public static final int COMPACT_ICON = 16;
	private static final int COMPACT_LEVEL_BG = 0xB0000000;

	/** Left edge and width of compact tab i, for a bar starting at x with width w. */
	private static int compactTabX(int x, int w, int i) {
		return x + COMPACT_LEVEL_W + COMPACT_GAP + i * (compactTabW(w) + COMPACT_GAP);
	}

	private static int compactTabW(int w) {
		int n = WitchercraftGuiLayout.NAV.length;
		return n <= 0 ? 0 : (w - COMPACT_LEVEL_W - COMPACT_GAP - (n - 1) * COMPACT_GAP) / n;
	}

	/**
	 * Draw the compact bar at (x, y), w wide, COMPACT_H tall, in SCREEN coords.
	 * Queues the hovered tab's label as a tooltip.
	 */
	public static void drawCompactNavbar(GuiGraphicsExtractor g, Font font, String activeTabId, int x, int y, int w, int mouseX, int mouseY) {
		drawCompactLevel(g, font, x, y);
		int tw = compactTabW(w), th = COMPACT_H;
		WitchercraftGuiLayout.Nav[] navs = WitchercraftGuiLayout.NAV;
		for (int i = 0; i < navs.length; i++) {
			WitchercraftGuiLayout.Nav nav = navs[i];
			boolean active = nav.pageId.equals(activeTabId);
			int tx = compactTabX(x, w, i);
			g.fill(tx, y, tx + tw, y + th, active ? TAB_BG_ACTIVE : TAB_BG);
			g.fill(tx, y, tx + tw, y + 1, TAB_BORDER);
			g.fill(tx, y + th - 1, tx + tw, y + th, TAB_BORDER);
			if (nav.icon != null && !nav.icon.isEmpty())
				g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(nav.icon), tx + (tw - COMPACT_ICON) / 2, y + (th - COMPACT_ICON) / 2, 0, 0, COMPACT_ICON, COMPACT_ICON, COMPACT_ICON, COMPACT_ICON);
			if (active)
				g.fill(tx + 2, y + th, tx + tw - 2, y + th + 1, TAB_ACCENT);
			if (mouseX >= tx && mouseX < tx + tw && mouseY >= y && mouseY < y + th)
				g.setComponentTooltipForNextFrame(font, java.util.List.of(navLabel(nav)), mouseX, mouseY);
		}
	}

	/** The pageId of the compact tab under a SCREEN point, or null. */
	public static String compactNavTabAt(int x, int y, int w, double mx, double my) {
		if (my < y || my >= y + COMPACT_H)
			return null;
		int tw = compactTabW(w);
		for (int i = 0; i < WitchercraftGuiLayout.NAV.length; i++) {
			int tx = compactTabX(x, w, i);
			if (mx >= tx && mx < tx + tw)
				return WitchercraftGuiLayout.NAV[i].pageId;
		}
		return null;
	}

	/** Level number over a thin XP bar, in a COMPACT_LEVEL_W x COMPACT_H box. */
	private static void drawCompactLevel(GuiGraphicsExtractor g, Font font, int x, int y) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		WitchercraftModVariables.PlayerVariables v = mc.player.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		int level = (int) Math.floor(v.witchercraftPlayerLevel);
		double req = v.witchercraftPlayerExperienceRequirement;
		double frac = req > 0 ? Math.max(0, Math.min(1, v.witchercraftPlayerExperience / req)) : 0;

		int w = COMPACT_LEVEL_W, h = COMPACT_H;
		g.fill(x, y, x + w, y + h, COMPACT_LEVEL_BG);
		Component num = Component.literal(Integer.toString(level));
		g.text(font, num, x + (w - font.width(num)) / 2, y + 3, WitchercraftGuiLayout.LEVEL_TEXT_COLOR, true);
		int bx = x + 2, by = y + h - 5, bw = w - 4;
		g.fill(bx, by, bx + bw, by + 2, WitchercraftGuiLayout.XP_BAR_BG_COLOR);
		int filled = (int) Math.round(frac * bw);
		if (filled > 0)
			g.fill(bx, by, bx + filled, by + 2, WitchercraftGuiLayout.XP_BAR_FILL_COLOR);
	}

	/**
	 * Level + XP readout, drawn in DESIGN-canvas coords (call inside the shell's
	 * design->screen transform). Reads the live, already-synced player vars, so it
	 * is pure client state and needs no packet. Also drawn by the reworked pause
	 * menu for visual coherence.
	 */
	public static void drawLevelReadout(GuiGraphicsExtractor g, Font font) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		WitchercraftModVariables.PlayerVariables v = mc.player.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		int level = (int) Math.floor(v.witchercraftPlayerLevel);
		double xp = v.witchercraftPlayerExperience;
		double req = v.witchercraftPlayerExperienceRequirement;
		double frac = req > 0 ? Math.max(0, Math.min(1, xp / req)) : 0;

		// level number (scaled)
		g.pose().pushMatrix();
		g.pose().translate(WitchercraftGuiLayout.LEVEL_X, WitchercraftGuiLayout.LEVEL_Y);
		g.pose().scale(WitchercraftGuiLayout.LEVEL_SCALE, WitchercraftGuiLayout.LEVEL_SCALE);
		g.text(font, Component.literal(Integer.toString(level)), 0, 0, WitchercraftGuiLayout.LEVEL_TEXT_COLOR, true);
		g.pose().popMatrix();

		// xp progress bar (empty track + left-anchored fill)
		int bx = WitchercraftGuiLayout.XP_BAR_X, by = WitchercraftGuiLayout.XP_BAR_Y;
		int bw = WitchercraftGuiLayout.XP_BAR_W, bh = WitchercraftGuiLayout.XP_BAR_H;
		g.fill(bx, by, bx + bw, by + bh, WitchercraftGuiLayout.XP_BAR_BG_COLOR);
		int filled = (int) Math.round(frac * bw);
		if (filled > 0)
			g.fill(bx, by, bx + filled, by + bh, WitchercraftGuiLayout.XP_BAR_FILL_COLOR);

		// exp/req caption
		java.text.DecimalFormat fmt = new java.text.DecimalFormat("0.##");
		Component caption = Component.literal(fmt.format(xp) + "/" + fmt.format(req));
		g.text(font, caption, WitchercraftGuiLayout.XP_TEXT_X, WitchercraftGuiLayout.XP_TEXT_Y, WitchercraftGuiLayout.XP_TEXT_COLOR, false);
	}

	// ---- content region in SCREEN coords ---------------------------------------
	// Container tabs place real slots at normal GUI scale, so they need the
	// content region (below the navbar) in screen pixels, not design pixels.

	public static int contentScreenX(int width, int height) {
		float s = layoutScale(width, height);
		return Math.round(offsetX(width, s) + WitchercraftGuiLayout.contentX() * s);
	}

	public static int contentScreenY(int width, int height) {
		float s = layoutScale(width, height);
		return Math.round(offsetY(height, s) + WitchercraftGuiLayout.contentY() * s);
	}

	public static int contentScreenW(int width, int height) {
		return Math.round(WitchercraftGuiLayout.contentW() * layoutScale(width, height));
	}

	public static int contentScreenH(int width, int height) {
		return Math.round(WitchercraftGuiLayout.contentH() * layoutScale(width, height));
	}
}

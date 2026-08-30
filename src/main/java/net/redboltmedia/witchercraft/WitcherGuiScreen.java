package net.redboltmedia.witchercraft;

import java.util.List;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

import net.minecraft.client.Minecraft;
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
 * The GUI shell - a persistent, client-only, FULLSCREEN Screen.
 *
 * It renders against a fixed virtual design canvas ({@link WitcherGuiLayout#DESIGN_W}
 * x {@code DESIGN_H}) which is scaled UNIFORMLY to fit the real screen, then drawn
 * centred with black letterbox bars filling any leftover (non-16:9) space. This
 * makes the UI independent of Minecraft's GUI-scale setting (it always fills the
 * same fraction of the physical screen) and never distorted. A background image
 * fills the design canvas; the navbar + active {@link GuiPage} draw on top.
 *
 * React model: the shell stays mounted; clicking a tab is {@code setState(activeTabId)}.
 * Opened with {@code Minecraft.setScreen}, optionally onto a chosen tab.
 *
 * HAND-MAINTAINED: no MCreator element, never regenerated.
 */
public class WitcherGuiScreen extends Screen {

	private static final int LETTERBOX = 0xFF000000; // opaque black behind everything
	private static final int PANEL_DIM = 0x33000000; // gentle darken over the bg image
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

	// ---- design-canvas <-> screen transform ----------------------------------

	/** Uniform scale to fit the 16:9 design canvas inside the (gui-scaled) screen. */
	private float layoutScale() {
		return Math.min((float) this.width / WitcherGuiLayout.DESIGN_W, (float) this.height / WitcherGuiLayout.DESIGN_H);
	}

	private float offsetX(float s) {
		return (this.width - WitcherGuiLayout.DESIGN_W * s) / 2f;
	}

	private float offsetY(float s) {
		return (this.height - WitcherGuiLayout.DESIGN_H * s) / 2f;
	}

	private int toDesignX(double sx, float ox, float s) {
		return (int) ((sx - ox) / s);
	}

	private int toDesignY(double sy, float oy, float s) {
		return (int) ((sy - oy) / s);
	}

	// ---- rendering -----------------------------------------------------------
	// This generator drives screens through extractBackground + extractRenderState
	// (both take GuiGraphicsExtractor), not a render(GuiGraphics) override.

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		// When the active page wants the world visible (meditation time-lapse),
		// draw nothing at all - pure live world behind the Screen (F1-clean look).
		if (activePage().wantsWorldVisible()) {
			return;
		}

		// opaque black base -> becomes the letterbox bars on non-16:9 screens
		g.fill(0, 0, this.width, this.height, LETTERBOX);

		float s = layoutScale();
		g.pose().pushMatrix();
		g.pose().translate(offsetX(s), offsetY(s));
		g.pose().scale(s, s);
		// background image fills the 16:9 design canvas (never stretched to the
		// screen aspect - black fills the rest)
		String bg = WitcherGuiLayout.backgroundFor(activeTabId);
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(bg), 0, 0, 0, 0, WitcherGuiLayout.DESIGN_W, WitcherGuiLayout.DESIGN_H, WitcherGuiLayout.DESIGN_W, WitcherGuiLayout.DESIGN_H);
		g.fill(0, 0, WitcherGuiLayout.DESIGN_W, WitcherGuiLayout.DESIGN_H, PANEL_DIM);
		g.pose().popMatrix();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		super.extractRenderState(g, mouseX, mouseY, partial); // widgets, if any

		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = toDesignX(mouseX, ox, s), dmy = toDesignY(mouseY, oy, s);

		g.pose().pushMatrix();
		g.pose().translate(ox, oy);
		g.pose().scale(s, s);
		// page fills the region below the navbar; navbar is drawn last (on top /
		// "reserved") so page content never covers the tabs. A page that wants the
		// world visible (meditation spin) also suppresses the navbar for the
		// F1-clean look.
		activePage().render(g, WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, partial);
		if (!activePage().wantsWorldVisible())
			drawNavbar(g);
		g.pose().popMatrix();

		// meditation fade overlay (screen space, on top of the world + dial): black
		// that fades in at the start of a spin and out at the end.
		float fade = activePage().spinFadeAlpha();
		if (fade > 0f)
			g.fill(0, 0, this.width, this.height, (int) (Math.min(1f, fade) * 255f) << 24);

		// tooltip in SCREEN space at the real cursor (after the transform is popped)
		List<Component> tip = activePage().pollTooltip();
		if (tip != null && !tip.isEmpty())
			g.setComponentTooltipForNextFrame(this.font, tip, mouseX, mouseY);
	}

	/**
	 * Level + XP readout, drawn in DESIGN-canvas coords (call inside the shell's
	 * design->screen transform). Reads the live, already-synced player vars, so it
	 * is pure client state and needs no packet. Static + self-contained so the
	 * reworked pause menu can draw the identical readout for visual coherence.
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
		g.pose().translate(WitcherGuiLayout.LEVEL_X, WitcherGuiLayout.LEVEL_Y);
		g.pose().scale(WitcherGuiLayout.LEVEL_SCALE, WitcherGuiLayout.LEVEL_SCALE);
		g.text(font, Component.literal(Integer.toString(level)), 0, 0, WitcherGuiLayout.LEVEL_TEXT_COLOR, true);
		g.pose().popMatrix();

		// xp progress bar (empty track + left-anchored fill)
		int bx = WitcherGuiLayout.XP_BAR_X, by = WitcherGuiLayout.XP_BAR_Y;
		int bw = WitcherGuiLayout.XP_BAR_W, bh = WitcherGuiLayout.XP_BAR_H;
		g.fill(bx, by, bx + bw, by + bh, WitcherGuiLayout.XP_BAR_BG_COLOR);
		int filled = (int) Math.round(frac * bw);
		if (filled > 0)
			g.fill(bx, by, bx + filled, by + bh, WitcherGuiLayout.XP_BAR_FILL_COLOR);

		// exp/req caption
		java.text.DecimalFormat fmt = new java.text.DecimalFormat("0.##");
		Component caption = Component.literal(fmt.format(xp) + "/" + fmt.format(req));
		g.text(font, caption, WitcherGuiLayout.XP_TEXT_X, WitcherGuiLayout.XP_TEXT_Y, WitcherGuiLayout.XP_TEXT_COLOR, false);
	}

	private void drawNavbar(GuiGraphicsExtractor g) {
		Font font = this.font;
		drawLevelReadout(g, font);
		int count = WitcherGuiLayout.NAV.length;
		for (int i = 0; i < count; i++) {
			WitcherGuiLayout.Nav nav = WitcherGuiLayout.NAV[i];
			boolean active = nav.pageId.equals(activeTabId);
			int tx = WitcherGuiLayout.navTabX(count, i);
			int ty = WitcherGuiLayout.NAV_Y;
			int tw = WitcherGuiLayout.NAV_TAB_W, th = WitcherGuiLayout.NAV_H;

			g.fill(tx, ty, tx + tw, ty + th, active ? TAB_BG_ACTIVE : TAB_BG);
			g.fill(tx, ty, tx + tw, ty + 1, TAB_BORDER);
			g.fill(tx, ty + th - 1, tx + tw, ty + th, TAB_BORDER);

			if (nav.icon != null && !nav.icon.isEmpty()) {
				int ix = tx + (tw - WitcherGuiLayout.NAV_ICON) / 2;
				g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(nav.icon), ix, ty + 2, 0, 0, WitcherGuiLayout.NAV_ICON, WitcherGuiLayout.NAV_ICON, WitcherGuiLayout.NAV_ICON, WitcherGuiLayout.NAV_ICON);
			}

			Component label = navLabel(nav);
			int lw = font.width(label);
			g.text(font, label, tx + (tw - lw) / 2, ty + th - 10, active ? TAB_TEXT_ACTIVE : TAB_TEXT, false);

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
		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = toDesignX(event.x(), ox, s), dmy = toDesignY(event.y(), oy, s);

		if (event.button() == 0 && !activePage().wantsWorldVisible()) {
			int count = WitcherGuiLayout.NAV.length;
			for (int i = 0; i < count; i++) {
				int tx = WitcherGuiLayout.navTabX(count, i);
				int ty = WitcherGuiLayout.NAV_Y;
				if (dmx >= tx && dmx < tx + WitcherGuiLayout.NAV_TAB_W && dmy >= ty && dmy < ty + WitcherGuiLayout.NAV_H) {
					String pid = WitcherGuiLayout.NAV[i].pageId;
					if (!pid.equals(activeTabId)) {
						activeTabId = pid;
						activePage().onShown();
					}
					return true;
				}
			}
		}
		if (activePage().mouseClicked(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, event.button()))
			return true;
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = toDesignX(event.x(), ox, s), dmy = toDesignY(event.y(), oy, s);
		if (activePage().mouseReleased(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, event.button()))
			return true;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = toDesignX(event.x(), ox, s), dmy = toDesignY(event.y(), oy, s);
		if (activePage().mouseDragged(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, event.button(), dragX / s, dragY / s))
			return true;
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = toDesignX(mouseX, ox, s), dmy = toDesignY(mouseY, oy, s);
		if (activePage().mouseScrolled(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, scrollX, scrollY))
			return true;
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (activePage().keyPressed(key))
			return true;
		return super.keyPressed(event); // Esc closes via onClose()
	}

	@Override
	public void tick() {
		super.tick();
		// a page can ask to close itself (meditation auto-closes after its fade-out)
		if (activePage().requestsClose())
			onClose();
	}

	@Override
	public void onClose() {
		activePage().onClose();
		this.minecraft.setScreen(null); // opened via setScreen, so close the same way
	}

	@Override
	public boolean isPauseScreen() {
		return activePage().pausesGame();
	}
}

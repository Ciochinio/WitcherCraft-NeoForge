package net.redboltmedia.witchercraft;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

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

	// ---- design-canvas <-> screen transform (shared with container tabs) ------

	private float layoutScale() {
		return ShellChrome.layoutScale(this.width, this.height);
	}

	private float offsetX(float s) {
		return ShellChrome.offsetX(this.width, s);
	}

	private float offsetY(float s) {
		return ShellChrome.offsetY(this.height, s);
	}

	private int toDesignX(double sx, float ox, float s) {
		return ShellChrome.toDesignX(sx, ox, s);
	}

	private int toDesignY(double sy, float oy, float s) {
		return ShellChrome.toDesignY(sy, oy, s);
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

		// letterbox + per-tab background image on the 16:9 design canvas
		ShellChrome.drawBackground(g, this.width, this.height, activeTabId);
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
			ShellChrome.drawNavbar(g, this.font, activeTabId);
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

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = toDesignX(event.x(), ox, s), dmy = toDesignY(event.y(), oy, s);

		if (event.button() == 0 && !activePage().wantsWorldVisible()) {
			String pid = ShellChrome.navTabAt(dmx, dmy);
			if (pid != null) {
				if (WitcherGuiPages.isContainerTab(pid)) {
					// needs a server menu: the server opens it and replaces this screen
					WitcherGuiPages.open(pid);
				} else if (!pid.equals(activeTabId)) {
					activePage().onHidden();
					activeTabId = pid;
					activePage().onShown();
				}
				return true;
			}
		}
		if (activePage().mouseClicked(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, event.button(), doubleClick))
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
	public boolean charTyped(CharacterEvent event) {
		if (activePage().charTyped(event.codepoint()))
			return true;
		return super.charTyped(event);
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

	/** Runs however the shell goes away (closed, replaced by another screen, disconnect). */
	@Override
	public void removed() {
		activePage().onHidden();
		super.removed();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

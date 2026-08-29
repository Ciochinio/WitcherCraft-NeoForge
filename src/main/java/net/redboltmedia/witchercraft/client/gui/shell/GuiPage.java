package net.redboltmedia.witchercraft.client.gui.shell;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * One "page" (React-style component) rendered inside {@link WitcherGuiScreen}.
 *
 * The shell stays mounted and owns the navbar + frame; it swaps which page's
 * {@link #render} fills the CONTENT REGION - the whole area below the navbar -
 * based on its {@code activeTab} state. Switching tabs is pure client state.
 *
 * Every page is handed the content region as (regionX, regionY, regionW, regionH)
 * in DESIGN-canvas coordinates (see {@link WitcherGuiLayout}). A page draws
 * itself within that box however it wants - a bespoke page (like {@link PerkPage})
 * maps its own fixed layout onto the region; a trivial page just centres text.
 * All rendering happens inside the shell's design->screen scale transform.
 */
public interface GuiPage {

	/** Stable id; must equal a {@link WitcherGuiLayout.Nav#pageId} to be reachable. */
	String id();

	/** Navbar label (fallback when the layout supplies none). */
	Component navLabel();

	/** Optional navbar icon texture, or {@code null} to draw the label only. */
	default Identifier navIcon() {
		return null;
	}

	/**
	 * Draw the page into the content region (design coords).
	 *
	 * @param g       graphics
	 * @param x       content region left (design x)
	 * @param y       content region top (design y)
	 * @param w       content region width
	 * @param h       content region height
	 * @param mouseX  mouse x in DESIGN coords (already mapped from screen)
	 * @param mouseY  mouse y in DESIGN coords
	 * @param partial partial tick
	 */
	void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial);

	/**
	 * Handle a click inside the content region. Region + mouse are DESIGN coords.
	 * Return true to consume.
	 */
	default boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		return false;
	}

	/** Handle a key press. Return true to consume (blocks default Esc-close, etc.). */
	default boolean keyPressed(int keyCode) {
		return false;
	}

	/**
	 * The tooltip lines to show this frame (computed during {@link #render}), one
	 * {@link Component} per line, or null/empty for none. The shell renders it in
	 * SCREEN space at the real cursor, so a page must not call
	 * setTooltipForNextFrame itself (the scale transform would misplace it).
	 */
	default List<Component> pollTooltip() {
		return null;
	}

	/**
	 * When true, the shell skips its opaque background image (and heavy dim) so
	 * the live world renders through the Screen. Used by the meditation page
	 * during the accelerated time-lapse, so the player watches the real sky spin.
	 */
	default boolean wantsWorldVisible() {
		return false;
	}

	/** Called when this page becomes the active tab. */
	default void onShown() {
	}

	/** Called when the shell closes while this page is active. */
	default void onClose() {
	}
}

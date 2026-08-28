package net.redboltmedia.witchercraft.client.gui.shell;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * One "page" (React-style component) rendered inside {@link WitcherGuiScreen}.
 *
 * The shell stays mounted and owns the navbar + frame; it swaps which page's
 * {@link #render} fills the content region based on its {@code activeTab} state.
 * Switching tabs is pure client state - no new screen, no server round-trip.
 *
 * All coordinates a page receives / uses are ABSOLUTE screen pixels: the shell
 * hands each call the content region's top-left as (originX, originY); a page
 * adds its own gui-local offsets to that. This mirrors how the old container
 * screen added leftPos / topPos.
 *
 * A page is identified by {@link #id}, which must match the {@code pageId} of a
 * {@link WitcherGuiLayout.Nav} entry for the tab to appear in the navbar. Pages
 * with no matching Nav entry simply never show; Nav entries with no page render
 * as an inert tab. This keeps "what pages exist" (code) separate from "how the
 * navbar looks / is ordered" (the tool-edited layout).
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
	 * Draw the page into the content region.
	 *
	 * @param g         graphics
	 * @param originX   absolute x of the content region's top-left
	 * @param originY   absolute y of the content region's top-left
	 * @param mouseX    absolute mouse x
	 * @param mouseY    absolute mouse y
	 * @param partial   partial tick
	 */
	void render(GuiGraphicsExtractor g, int originX, int originY, int mouseX, int mouseY, float partial);

	/**
	 * Handle a click inside the shell. Coordinates are absolute; subtract
	 * (originX, originY) for gui-local. Return true to consume.
	 */
	default boolean mouseClicked(int originX, int originY, double mouseX, double mouseY, int button) {
		return false;
	}

	/** Handle a key press. Return true to consume (blocks default Esc-close, etc.). */
	default boolean keyPressed(int keyCode) {
		return false;
	}

	/** Called when this page becomes the active tab. */
	default void onShown() {
	}

	/** Called when the shell closes while this page is active. */
	default void onClose() {
	}
}

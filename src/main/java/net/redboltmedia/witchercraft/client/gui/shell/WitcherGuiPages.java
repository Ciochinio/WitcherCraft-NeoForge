package net.redboltmedia.witchercraft.client.gui.shell;

import java.util.HashMap;
import java.util.Map;

/**
 * The "route table": maps a pageId to the {@link GuiPage} that renders it.
 *
 * The navbar is driven by {@link WitcherGuiLayout#NAV} (order + visuals); this
 * class only answers "given a pageId, which page object handles it?". Any nav
 * pageId without a dedicated page class falls back to a generic
 * {@link LayoutPage} that renders that page's {@link WitcherGuiLayout} boxes -
 * so adding a placeholder tab is a one-line edit in the tool, no new class.
 *
 * To add a real (custom-rendered) page: implement {@link GuiPage} and register
 * its singleton in {@link #CUSTOM} below.
 */
public final class WitcherGuiPages {
	private WitcherGuiPages() {
	}

	/** Custom pages keyed by id. Everything else becomes a LayoutPage. */
	private static final Map<String, GuiPage> CUSTOM = new HashMap<>();

	/** Cache of auto-built placeholder pages, so each id is one stable object. */
	private static final Map<String, GuiPage> LAYOUT_CACHE = new HashMap<>();

	static {
		// The ported perk equip/tree screen, now a page under the "skills" tab.
		PerkPage perk = new PerkPage("skills");
		CUSTOM.put(perk.id(), perk);
	}

	/** The page handling a given id (never null - falls back to a LayoutPage). */
	public static GuiPage forId(String pageId) {
		GuiPage custom = CUSTOM.get(pageId);
		if (custom != null)
			return custom;
		return LAYOUT_CACHE.computeIfAbsent(pageId, LayoutPage::new);
	}

	/** The first navbar tab's pageId, used as the default active tab. */
	public static String defaultPageId() {
		return WitcherGuiLayout.NAV.length > 0 ? WitcherGuiLayout.NAV[0].pageId : "";
	}
}

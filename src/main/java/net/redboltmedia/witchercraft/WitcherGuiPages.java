package net.redboltmedia.witchercraft;

import java.util.HashMap;
import java.util.Map;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;

/**
 * The "route table": maps a pageId to the {@link GuiPage} that renders it.
 *
 * The navbar is driven by {@link WitcherGuiLayout#NAV} (order + visuals); this
 * class only answers "given a pageId, which page object handles it?". Any nav
 * pageId without a dedicated page class falls back to a generic
 * {@link LayoutPage} that renders that page's {@link WitcherGuiLayout} boxes -
 * so adding a placeholder tab is a one-line edit in the tool, no new class.
 *
 * To add a real (custom-rendered) page: implement {@link GuiPage} (its own class
 * + its own placer tool, like Skills) and register its singleton in {@link #CUSTOM}.
 */
public final class WitcherGuiPages {
	private WitcherGuiPages() {
	}

	/** Bespoke pages keyed by id. Everything else is a "coming soon" placeholder. */
	private static final Map<String, GuiPage> CUSTOM = new HashMap<>();

	/** Cache of placeholder pages, so each id is one stable object. */
	private static final Map<String, GuiPage> PLACEHOLDER_CACHE = new HashMap<>();

	static {
		// The perk tree + equip grid. "skills" is a container tab, so SkillsScreen
		// (not WitcherGuiScreen) draws this page around the real mutagen slots.
		PerkPage perk = new PerkPage("skills");
		CUSTOM.put(perk.id(), perk);

		// The meditation clock dial.
		MeditationPage meditation = new MeditationPage("meditation");
		CUSTOM.put(meditation.id(), meditation);

		MapPage map = new MapPage();
		CUSTOM.put(map.id(), map);
	}

	/** The page handling a given id (never null - falls back to a PlaceholderPage). */
	public static GuiPage forId(String pageId) {
		GuiPage custom = CUSTOM.get(pageId);
		if (custom != null)
			return custom;
		return PLACEHOLDER_CACHE.computeIfAbsent(pageId, PlaceholderPage::new);
	}

	/** The first navbar tab's pageId, used as the default active tab. */
	public static String defaultPageId() {
		return WitcherGuiLayout.NAV.length > 0 ? WitcherGuiLayout.NAV[0].pageId : "";
	}

	/**
	 * Tabs backed by a server-side container menu (real item slots). They are not
	 * {@link GuiPage}s: the server opens the menu and its own screen draws the
	 * shell chrome through {@link ShellChrome}.
	 */
	public static boolean isContainerTab(String pageId) {
		return "alchemy".equals(pageId) || "skills".equals(pageId);
	}

	/**
	 * Tabs that are a screen of their own rather than a page inside
	 * {@link WitcherGuiScreen}: the container tabs, plus Inventory, which is the
	 * decorated vanilla {@link net.minecraft.client.gui.screens.inventory.InventoryScreen}
	 * ({@link InventoryShell}).
	 */
	public static boolean hasOwnScreen(String pageId) {
		return isContainerTab(pageId) || InventoryShell.TAB_ID.equals(pageId);
	}

	/**
	 * THE routing point for opening the shell on a tab (nav clicks, P, per-tab
	 * keys, the pause menu). A container tab asks the server to open its menu,
	 * Inventory opens the vanilla inventory, and any other tab opens
	 * {@link WitcherGuiScreen} directly.
	 */
	public static void open(String pageId) {
		if (InventoryShell.TAB_ID.equals(pageId))
			InventoryShell.open();
		else if ("alchemy".equals(pageId))
			ClientPacketDistributor.sendToServer(AlchemyOpenMessage.INSTANCE);
		else if ("skills".equals(pageId))
			ClientPacketDistributor.sendToServer(SkillsOpenMessage.INSTANCE);
		else
			Minecraft.getInstance().setScreen(new WitcherGuiScreen(pageId));
	}
}

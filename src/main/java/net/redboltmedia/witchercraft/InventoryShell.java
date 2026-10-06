package net.redboltmedia.witchercraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

/**
 * The Inventory tab of the GUI shell. Unlike every other tab it is NOT our own
 * screen: it is the vanilla {@link InventoryScreen} (E, the I key and the navbar
 * all open the same screen), decorated from the outside so that mods hooking the
 * vanilla inventory (recipe book, accessory buttons, JEI, mod-added slots) keep
 * working. Never replace or subclass it.
 *
 * - Background and navbar: drawn by
 *   {@link net.redboltmedia.witchercraft.mixin.InventoryShellBackgroundMixin} in
 *   place of vanilla's dark gradient, so they sit under the panel and tooltips.
 *   NeoForge has no event between that gradient and the panel blit.
 * - Navbar style: the full shell navbar, or the compact bar above the panel when
 *   an item-list overlay mod owns the side strips (client config
 *   {@code inventory.navbar}; see {@link #compact()}).
 * - Navbar clicks: {@link ScreenEvent.MouseButtonPressed.Pre} here.
 * - Damage interrupt: {@link WitcherGuiDamageInterrupt} closes it like E does.
 *
 * Only the exact InventoryScreen class is decorated; creative and subclasses
 * from other mods are left alone.
 *
 * HAND-MAINTAINED: not an MCreator element.
 */
public final class InventoryShell {
	private InventoryShell() {
	}

	public static final String TAB_ID = "inventory";

	/**
	 * Item-list overlay mods that lay out in the strips beside a container GUI,
	 * over where the full navbar sits. Checked by mod id only (no dependency).
	 */
	private static final String[] OVERLAY_MODS = { "jei", "emi", "roughlyenoughitems" };
	private static Boolean overlayModLoaded;

	/** Last mouse position over the decorated inventory, for compact-bar tooltips. */
	private static int mouseX, mouseY;

	/** True for the screen this class decorates. */
	public static boolean isDecorated(Screen screen) {
		return screen != null && screen.getClass() == InventoryScreen.class;
	}

	/** Whether to draw the compact bar instead of the full navbar. */
	public static boolean compact() {
		return switch (WorldMapClientConfig.inventoryNavbar()) {
			case FULL -> false;
			case COMPACT -> true;
			case AUTO -> overlayModLoaded();
		};
	}

	private static boolean overlayModLoaded() {
		if (overlayModLoaded == null) {
			boolean any = false;
			for (String id : OVERLAY_MODS)
				any |= ModList.get().isLoaded(id);
			overlayModLoaded = any;
		}
		return overlayModLoaded;
	}

	/** Compact bar origin: the panel's column, at the full navbar's height. */
	private static int compactY(AbstractContainerScreen<?> screen) {
		// at the top like the full navbar, but never lower than just above the panel
		float s = ShellChrome.layoutScale(screen.width, screen.height);
		int top = Math.round(ShellChrome.offsetY(screen.height, s) + WitcherGuiLayout.NAV_Y * s);
		int abovePanel = screen.getTopPos() - ShellChrome.COMPACT_GAP - ShellChrome.COMPACT_H;
		return Math.max(0, Math.min(top, abovePanel));
	}

	/** Shell background plus the navbar, in place of vanilla's dim. */
	public static void drawBackground(GuiGraphicsExtractor g, Screen screen) {
		Minecraft mc = Minecraft.getInstance();
		ShellChrome.drawBackground(g, screen.width, screen.height, TAB_ID);
		if (compact() && screen instanceof AbstractContainerScreen<?> cs) {
			ShellChrome.drawCompactNavbar(g, mc.font, TAB_ID, cs.getLeftPos(), compactY(cs), cs.getImageWidth(), mouseX, mouseY);
		} else {
			ShellChrome.pushDesignTransform(g, screen.width, screen.height);
			ShellChrome.drawNavbar(g, mc.font, TAB_ID);
			g.pose().popMatrix();
		}
	}

	/** Open the inventory exactly as vanilla's E key does. */
	public static void open() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		if (mc.gameMode != null && mc.gameMode.isServerControlledInventory()) {
			mc.player.sendOpenInventory();
		} else {
			mc.getTutorial().onOpenInventory();
			mc.setScreen(new InventoryScreen(mc.player));
		}
	}

	/** Close the inventory the way E does (the server returns the crafting grid). */
	public static void close() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && isDecorated(mc.screen))
			mc.player.closeContainer();
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class Input {
		private static boolean swallowRelease;

		@SubscribeEvent
		public static void render(ScreenEvent.Render.Pre event) {
			if (isDecorated(event.getScreen())) {
				mouseX = event.getMouseX();
				mouseY = event.getMouseY();
			}
		}

		@SubscribeEvent
		public static void mousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
			Screen screen = event.getScreen();
			if (event.getButton() != 0 || !isDecorated(screen))
				return;
			String pid = tabAt((AbstractContainerScreen<?>) screen, event.getMouseX(), event.getMouseY());
			if (pid == null)
				return;
			event.setCanceled(true);
			swallowRelease = true;
			if (!pid.equals(TAB_ID))
				switchToTab(pid);
		}

		@SubscribeEvent
		public static void mouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
			// the release of a navbar click must not reach the container logic
			if (swallowRelease) {
				swallowRelease = false;
				event.setCanceled(true);
			}
		}

		private static String tabAt(AbstractContainerScreen<?> screen, double mx, double my) {
			if (compact())
				return ShellChrome.compactNavTabAt(screen.getLeftPos(), compactY(screen), screen.getImageWidth(), mx, my);
			float s = ShellChrome.layoutScale(screen.width, screen.height);
			int dmx = ShellChrome.toDesignX(mx, ShellChrome.offsetX(screen.width, s), s);
			int dmy = ShellChrome.toDesignY(my, ShellChrome.offsetY(screen.height, s), s);
			return ShellChrome.navTabAt(dmx, dmy);
		}

		/**
		 * Close the inventory menu on the server (it returns the crafting grid and
		 * the carried item) and open another tab without dropping to the world in
		 * between, same as {@link SkillsScreen}.
		 */
		private static void switchToTab(String pageId) {
			Minecraft mc = Minecraft.getInstance();
			mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
			mc.player.containerMenu = mc.player.inventoryMenu;
			WitcherGuiPages.open(pageId);
		}
	}
}

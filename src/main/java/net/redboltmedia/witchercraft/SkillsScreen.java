package net.redboltmedia.witchercraft;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The Skills tab. A container screen for {@link SkillsMenu} that draws the shell
 * chrome ({@link ShellChrome}: letterboxed background and navbar, scaled with
 * the design canvas) and, at normal GUI scale, the perk panel ({@link PerkPage}:
 * tabs, tree, equip grid, socket frames) plus the menu's real slots.
 *
 * The 360x200 panel is drawn 1:1 in GUI pixels and centred in the content
 * region below the navbar, like Alchemy's slot block. leftPos / topPos are the
 * panel origin, and every slot position comes from {@link PerkEquipLayout}, so
 * each item sits exactly in its 18x18 frame and vanilla handles hover, clicks,
 * shift-click, drag-splitting and the carried item. The panel is outlined red
 * when it does not fit the content region (GUI scale too big for the window).
 *
 * Clicks go to the navbar first, then to a slot under the cursor, then to the
 * perk page. The inventory slots are only active while the Mutagens sub-tab is
 * shown.
 *
 * HAND-MAINTAINED: locked code element. The screen is bound to the menu type
 * here, not through an MCreator gui element.
 */
public class SkillsScreen extends AbstractContainerScreen<SkillsMenu> {
	private static final String TAB_ID = "skills";
	private static final int MISFIT_COLOR = 0xFFFF3030;

	private final PerkPage page;
	private boolean swallowRelease;
	private boolean fits;

	public SkillsScreen(SkillsMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, PerkEquipLayout.PANEL_W, PerkEquipLayout.PANEL_H);
		this.page = (PerkPage) WitcherGuiPages.forId(TAB_ID);
		this.page.setMutagenSource(gi -> menu.getSlot(gi).getItem());
		menu.inventoryShown = page.mutagensShown();
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class Registration {
		@SubscribeEvent
		public static void registerScreen(RegisterMenuScreensEvent event) {
			event.register(SkillsMenu.TYPE.get(), SkillsScreen::new);
		}
	}

	// ---- placement ---------------------------------------------------------------

	@Override
	protected void init() {
		super.init();
		int cx = contentX(), cy = contentY(), cw = contentW(), ch = contentH();
		int[] origin = PerkPage.origin(cx, cy, cw, ch);
		this.leftPos = origin[0];
		this.topPos = origin[1];
		this.fits = this.imageWidth <= cw && this.imageHeight <= ch;
	}

	private int contentX() {
		return ShellChrome.contentScreenX(this.width, this.height);
	}

	private int contentY() {
		return ShellChrome.contentScreenY(this.width, this.height);
	}

	private int contentW() {
		return ShellChrome.contentScreenW(this.width, this.height);
	}

	private int contentH() {
		return ShellChrome.contentScreenH(this.width, this.height);
	}

	// ---- rendering -----------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		this.menu.inventoryShown = page.mutagensShown();

		ShellChrome.drawBackground(g, this.width, this.height, TAB_ID);
		ShellChrome.pushDesignTransform(g, this.width, this.height);
		ShellChrome.drawNavbar(g, this.font, TAB_ID);
		g.pose().popMatrix();

		// the perk panel at 1:1 GUI scale (same origin as leftPos / topPos)
		page.render(g, contentX(), contentY(), contentW(), contentH(), mouseX, mouseY, partial);
		if (!fits)
			outline(g, leftPos, topPos, imageWidth, imageHeight, MISFIT_COLOR);

		// inventory slot frames (the socket frames are part of the perk page)
		if (this.menu.inventoryShown) {
			Identifier frame = Identifier.parse(AlchemyLayout.SLOT_INVENTORY_TEX);
			int size = AlchemyLayout.SLOT_TEX_SIZE;
			for (int i = SkillsMenu.INVENTORY_START; i < SkillsMenu.SLOT_END; i++) {
				Slot slot = this.menu.getSlot(i);
				g.blit(RenderPipelines.GUI_TEXTURED, frame, leftPos + slot.x - 1, topPos + slot.y - 1, 0, 0, size, size, size, size);
			}
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		super.extractRenderState(g, mouseX, mouseY, partial);
		// perk tooltips, unless a slot item's own tooltip or a carried item is showing
		boolean slotTooltip = this.hoveredSlot != null && this.hoveredSlot.hasItem();
		List<Component> tip = page.pollTooltip();
		if (tip != null && !tip.isEmpty() && !slotTooltip && this.menu.getCarried().isEmpty())
			g.setComponentTooltipForNextFrame(this.font, tip, mouseX, mouseY);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		// no vanilla title/inventory labels
	}

	private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x, y, x + w, y + 1, color);
		g.fill(x, y + h - 1, x + w, y + h, color);
		g.fill(x, y, x + 1, y + h, color);
		g.fill(x + w - 1, y, x + w, y + h, color);
	}

	// ---- input -----------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			float s = ShellChrome.layoutScale(this.width, this.height);
			int dmx = ShellChrome.toDesignX(event.x(), ShellChrome.offsetX(this.width, s), s);
			int dmy = ShellChrome.toDesignY(event.y(), ShellChrome.offsetY(this.height, s), s);
			String pid = ShellChrome.navTabAt(dmx, dmy);
			if (pid != null) {
				swallowRelease = true;
				if (!pid.equals(TAB_ID))
					switchToTab(pid);
				return true;
			}
		}
		if (slotAt(event.x(), event.y()) == null
				&& page.mouseClicked(contentX(), contentY(), contentW(), contentH(), event.x(), event.y(), event.button(), doubleClick)) {
			swallowRelease = true;
			this.menu.inventoryShown = page.mutagensShown();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		// the release of a navbar or perk click must not reach the container logic
		if (swallowRelease) {
			swallowRelease = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		// Esc drops a held perk before it closes the screen
		if (page.keyPressed(InputConstants.getKey(event).getValue()))
			return true;
		return super.keyPressed(event);
	}

	@Override
	protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
		// the whole screen is the tab: a click on empty space never throws the carried item
		return false;
	}

	/** The active slot under a screen point, or null (same bounds as vanilla's hover test). */
	private Slot slotAt(double mx, double my) {
		for (Slot slot : this.menu.slots) {
			int x = leftPos + slot.x, y = topPos + slot.y;
			if (slot.isActive() && mx >= x - 1 && mx < x + 17 && my >= y - 1 && my < y + 17)
				return slot;
		}
		return null;
	}

	/**
	 * Close the menu on the server and show the shell on another tab, without
	 * dropping to the world in between (same as {@link AlchemyScreen}).
	 */
	private void switchToTab(String pageId) {
		this.minecraft.getConnection().send(new ServerboundContainerClosePacket(this.menu.containerId));
		this.minecraft.player.containerMenu = this.minecraft.player.inventoryMenu;
		if (WitcherGuiPages.isContainerTab(pageId))
			WitcherGuiPages.open(pageId); // another container tab: the server replaces this screen
		else
			this.minecraft.setScreen(new WitcherGuiScreen(pageId));
	}
}

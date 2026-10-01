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
 * chrome ({@link ShellChrome}) and the perk page ({@link PerkPage}: tree, equip
 * grid, socket frames) exactly as the shell did, plus the menu's real slots.
 *
 * The perk page scales with the design canvas and its own fit scale, so the
 * slots follow it: on every resize each mutagen slot is moved to the centre of
 * its socket frame, and the inventory is centred in the tree area the Mutagens
 * sub-tab leaves empty (Slot.x / y are writable through the access transformer).
 * The slots themselves stay at normal GUI scale, so vanilla handles hover,
 * clicks, shift-click, drag-splitting and the carried item. leftPos / topPos are
 * 0, so slot positions are screen coordinates.
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
	private static final int SLOT = 18; // vanilla slot pitch
	private static final int HOTBAR_GAP = 4;

	private final PerkPage page;
	private boolean swallowRelease;

	public SkillsScreen(SkillsMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
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
		this.leftPos = 0;
		this.topPos = 0;
		placeSlots();
	}

	/** {originX, originY, scale} mapping perk-local coords to screen coords. */
	private float[] perkToScreen() {
		float s = ShellChrome.layoutScale(this.width, this.height);
		float[] fit = PerkPage.layout(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH());
		return new float[]{ShellChrome.offsetX(this.width, s) + fit[0] * s, ShellChrome.offsetY(this.height, s) + fit[1] * s, fit[2] * s};
	}

	private void placeSlots() {
		float[] t = perkToScreen();

		// each mutagen slot centred on its socket frame
		for (int gi = 0; gi < SkillsMenu.MUTAGEN_SLOTS; gi++) {
			Slot slot = this.menu.getSlot(gi);
			float cx = t[0] + (PerkEquipLayout.SOCKET_X[gi] + PerkEquipLayout.SOCKET_SIZE / 2f) * t[2];
			float cy = t[1] + (PerkEquipLayout.SOCKET_Y[gi] + PerkEquipLayout.SOCKET_SIZE / 2f) * t[2];
			slot.x = Math.round(cx) - 8;
			slot.y = Math.round(cy) - 8;
		}

		// the inventory (3 rows, a gap, the hotbar) centred in the tree area
		int gridW = 9 * SLOT, gridH = 4 * SLOT + HOTBAR_GAP;
		float areaX = t[0] + PerkPage.INVENTORY_AREA_X * t[2], areaY = t[1] + PerkPage.INVENTORY_AREA_Y * t[2];
		float areaW = PerkPage.INVENTORY_AREA_W * t[2], areaH = PerkPage.INVENTORY_AREA_H * t[2];
		int left = Math.round(areaX + (areaW - gridW) / 2f) + 1;
		int top = Math.round(areaY + (areaH - gridH) / 2f) + 1;
		for (int i = 0; i < 36; i++) {
			Slot slot = this.menu.getSlot(SkillsMenu.INVENTORY_START + i);
			int row = i / 9, col = i % 9;
			slot.x = left + col * SLOT;
			slot.y = top + row * SLOT + (row == 3 ? HOTBAR_GAP : 0);
		}
	}

	// ---- rendering -----------------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
		this.menu.inventoryShown = page.mutagensShown();

		ShellChrome.drawBackground(g, this.width, this.height, TAB_ID);
		float s = ShellChrome.layoutScale(this.width, this.height);
		int dmx = ShellChrome.toDesignX(mouseX, ShellChrome.offsetX(this.width, s), s);
		int dmy = ShellChrome.toDesignY(mouseY, ShellChrome.offsetY(this.height, s), s);
		ShellChrome.pushDesignTransform(g, this.width, this.height);
		page.render(g, WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, partial);
		ShellChrome.drawNavbar(g, this.font, TAB_ID);
		g.pose().popMatrix();

		// inventory slot frames (the socket frames are part of the perk page)
		if (this.menu.inventoryShown) {
			Identifier frame = Identifier.parse(AlchemyLayout.SLOT_INVENTORY_TEX);
			int size = AlchemyLayout.SLOT_TEX_SIZE;
			for (int i = SkillsMenu.INVENTORY_START; i < SkillsMenu.SLOT_END; i++) {
				Slot slot = this.menu.getSlot(i);
				g.blit(RenderPipelines.GUI_TEXTURED, frame, slot.x - 1, slot.y - 1, 0, 0, size, size, size, size);
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

	// ---- input -----------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		float s = ShellChrome.layoutScale(this.width, this.height);
		int dmx = ShellChrome.toDesignX(event.x(), ShellChrome.offsetX(this.width, s), s);
		int dmy = ShellChrome.toDesignY(event.y(), ShellChrome.offsetY(this.height, s), s);
		if (event.button() == 0) {
			String pid = ShellChrome.navTabAt(dmx, dmy);
			if (pid != null) {
				swallowRelease = true;
				if (!pid.equals(TAB_ID))
					switchToTab(pid);
				return true;
			}
		}
		if (slotAt(event.x(), event.y()) == null
				&& page.mouseClicked(WitcherGuiLayout.contentX(), WitcherGuiLayout.contentY(), WitcherGuiLayout.contentW(), WitcherGuiLayout.contentH(), dmx, dmy, event.button(), doubleClick)) {
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
			if (slot.isActive() && mx >= slot.x - 1 && mx < slot.x + 17 && my >= slot.y - 1 && my < slot.y + 17)
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

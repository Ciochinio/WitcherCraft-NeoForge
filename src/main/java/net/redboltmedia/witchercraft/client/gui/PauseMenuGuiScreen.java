package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.PauseMenuGuiMenu;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;
import net.redboltmedia.witchercraft.WitcherGuiScreen;
import net.redboltmedia.witchercraft.WitcherGuiLayout;
import net.redboltmedia.witchercraft.PauseMenuLayout;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * The WitcherCraft pause menu - a launcher that works just like the shell
 * navbar, only BIGGER, drawn over its own background with the same level/XP
 * readout, so pausing and opening the shell read as one interface. Each tab
 * opens the shell on the matching page.
 *
 * It stays an MCreator container screen (locked_code) purely to keep the
 * existing open/back plumbing intact - the keybind opens PauseMenuGuiMenu on the
 * server, and the sub-GUIs' "back" buttons reopen it via
 * PauseMenuGuiBackButtonProcedure. But it no longer uses vanilla Button widgets
 * or the 176x166 container texture: it renders fullscreen against the shell's
 * virtual design canvas and hit-tests its own tabs.
 *
 * All geometry, background and the tab list live in {@link PauseMenuLayout}
 * (data-driven, edited in tools/pause-menu-creator.html); this class is just
 * render + input. Clicking a tab opens {@code new WitcherGuiScreen(pageId)}
 * client-side (opening a screen is not server-authoritative state, see TDD 3.9).
 * The old Alchemy/Glossary/Character/Bestiary container GUIs are reached via
 * commands now, not this menu.
 */
public class PauseMenuGuiScreen extends AbstractContainerScreen<PauseMenuGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	// shell-matching palette (mirrors WitcherGuiScreen's private constants)
	private static final int LETTERBOX = 0xFF000000;
	private static final int PANEL_DIM = 0x33000000;
	private static final int TAB_BG = 0x66101015;
	private static final int TAB_BG_HOVER = 0xB0000000;
	private static final int TAB_BORDER = 0xFF33333D;
	private static final int TAB_TEXT = 0xFFC9C9D2;
	private static final int TAB_TEXT_HOVER = 0xFFFFFFFF;
	private static final int TAB_ACCENT = 0xFFFFDD55;

	public PauseMenuGuiScreen(PauseMenuGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 176, 166);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void init() {
		super.init(); // container geometry only; no vanilla Button widgets
	}

	// ---- design-canvas <-> screen transform (identical to the shell) ---------

	private float layoutScale() {
		return Math.min((float) this.width / PauseMenuLayout.DESIGN_W, (float) this.height / PauseMenuLayout.DESIGN_H);
	}

	private float offsetX(float s) {
		return (this.width - PauseMenuLayout.DESIGN_W * s) / 2f;
	}

	private float offsetY(float s) {
		return (this.height - PauseMenuLayout.DESIGN_H * s) / 2f;
	}

	// ---- rendering -----------------------------------------------------------

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		// opaque black base -> letterbox bars on non-16:9 screens (no super: we
		// deliberately do NOT draw the 176x166 container texture)
		g.fill(0, 0, this.width, this.height, LETTERBOX);

		float s = layoutScale();
		g.pose().pushMatrix();
		g.pose().translate(offsetX(s), offsetY(s));
		g.pose().scale(s, s);
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(PauseMenuLayout.BG), 0, 0, 0, 0, PauseMenuLayout.DESIGN_W, PauseMenuLayout.DESIGN_H, PauseMenuLayout.DESIGN_W, PauseMenuLayout.DESIGN_H);
		g.fill(0, 0, PauseMenuLayout.DESIGN_W, PauseMenuLayout.DESIGN_H, PANEL_DIM);
		g.pose().popMatrix();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		float s = layoutScale();
		float ox = offsetX(s), oy = offsetY(s);
		int dmx = (int) ((mouseX - ox) / s), dmy = (int) ((mouseY - oy) / s);

		g.pose().pushMatrix();
		g.pose().translate(ox, oy);
		g.pose().scale(s, s);

		// same readout as the navbar, same coords -> visually continuous
		WitcherGuiScreen.drawLevelReadout(g, this.font);
		drawNav(g, dmx, dmy);

		g.pose().popMatrix();
	}

	private void drawNav(GuiGraphicsExtractor g, int dmx, int dmy) {
		Font font = this.font;
		int count = PauseMenuLayout.NAV.length;
		int tw = PauseMenuLayout.NAV_TAB_W, th = PauseMenuLayout.NAV_H, icon = PauseMenuLayout.NAV_ICON;
		for (int i = 0; i < count; i++) {
			PauseMenuLayout.Nav nav = PauseMenuLayout.NAV[i];
			int tx = PauseMenuLayout.navTabX(count, i);
			int ty = PauseMenuLayout.NAV_Y;
			boolean hover = dmx >= tx && dmx < tx + tw && dmy >= ty && dmy < ty + th;

			g.fill(tx, ty, tx + tw, ty + th, hover ? TAB_BG_HOVER : TAB_BG);
			g.fill(tx, ty, tx + tw, ty + 1, TAB_BORDER);
			g.fill(tx, ty + th - 1, tx + tw, ty + th, TAB_BORDER);

			if (nav.icon != null && !nav.icon.isEmpty()) {
				int ix = tx + (tw - icon) / 2;
				g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(nav.icon), ix, ty + 4, 0, 0, icon, icon, icon, icon);
			}

			Component label = navLabel(nav);
			int lw = font.width(label);
			g.text(font, label, tx + (tw - lw) / 2, ty + th - 11, hover ? TAB_TEXT_HOVER : TAB_TEXT, false);

			if (hover)
				g.fill(tx + 3, ty + th, tx + tw - 3, ty + th + 1, TAB_ACCENT);
		}
	}

	private Component navLabel(PauseMenuLayout.Nav nav) {
		if (nav.labelKey != null && !nav.labelKey.isEmpty())
			return Component.translatable(nav.labelKey);
		return Component.literal(nav.pageId);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			float s = layoutScale();
			int dmx = (int) ((event.x() - offsetX(s)) / s), dmy = (int) ((event.y() - offsetY(s)) / s);
			int count = PauseMenuLayout.NAV.length;
			for (int i = 0; i < count; i++) {
				int tx = PauseMenuLayout.navTabX(count, i);
				int ty = PauseMenuLayout.NAV_Y;
				if (dmx >= tx && dmx < tx + PauseMenuLayout.NAV_TAB_W && dmy >= ty && dmy < ty + PauseMenuLayout.NAV_H) {
					// opening a screen is not server-authoritative state (see TDD 3.9)
					Minecraft.getInstance().setScreen(new WitcherGuiScreen(PauseMenuLayout.NAV[i].pageId));
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(event);
	}
}

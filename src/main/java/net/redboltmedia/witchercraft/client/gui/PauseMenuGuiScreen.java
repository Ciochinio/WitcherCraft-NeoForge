package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.PauseMenuGuiMenu;
import net.redboltmedia.witchercraft.network.PauseMenuGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;
import net.redboltmedia.witchercraft.WitcherGuiScreen;
import net.redboltmedia.witchercraft.WitcherGuiLayout;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * The WitcherCraft pause menu - a launcher styled to match the GUI shell's
 * navbar (same dark letterbox design canvas, same tab palette, and the same
 * level/XP readout top-left), so pressing pause and opening the shell read as
 * one interface.
 *
 * It stays an MCreator container screen (locked_code) purely to keep the
 * existing open/back plumbing intact - the keybind opens PauseMenuGuiMenu on the
 * server, and the sub-GUIs' "back" buttons reopen it via
 * PauseMenuGuiBackButtonProcedure. But it no longer uses vanilla Button widgets
 * or the 176x166 container texture: it renders fullscreen against the shell's
 * virtual design canvas and hit-tests its own tab-styled entries.
 *
 * Routing (see ENTRIES):
 *   - Meditation / Skills / Alchemy / Glossary -> open the shell tab directly,
 *     client-side (Minecraft.setScreen), no server round-trip.
 *   - Bestiary -> still a server-opened container (no shell page yet), sent the
 *     same way the old button did (PauseMenuGuiButtonMessage buttonID 4).
 *   - Character -> intentionally absent; it becomes a page inside the Inventory
 *     tab later. CharacterGui is kept (disconnected) for testing.
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
	private static final int TAB_BG_ACTIVE = 0xB0000000;
	private static final int TAB_BORDER = 0xFF33333D;
	private static final int TAB_TEXT = 0xFFC9C9D2;
	private static final int TAB_TEXT_ACTIVE = 0xFFFFFFFF;
	private static final int TAB_ACCENT = 0xFFFFDD55;

	// entry list geometry (design-canvas pixels)
	private static final int ENTRY_W = 200;
	private static final int ENTRY_H = 28;
	private static final int ENTRY_GAP = 8;
	private static final int ENTRY_ICON = 20;

	/** A launcher row: opens a shell tab (shellPage != null) or the Bestiary container. */
	private static final class Entry {
		final String labelKey;
		final String icon; // texture path, or "" for none
		final String shellPage; // shell tab id, or null
		final boolean bestiary; // server-opened Bestiary container

		Entry(String labelKey, String icon, String shellPage, boolean bestiary) {
			this.labelKey = labelKey;
			this.icon = icon;
			this.shellPage = shellPage;
			this.bestiary = bestiary;
		}
	}

	private static final Entry[] ENTRIES = {
			new Entry("gui.witchercraft.shell.nav.skills", "witchercraft:textures/gui/nav/skills.png", "skills", false),
			new Entry("gui.witchercraft.shell.nav.alchemy", "witchercraft:textures/gui/nav/alchemy.png", "alchemy", false),
			new Entry("gui.witchercraft.shell.nav.glossary", "witchercraft:textures/gui/nav/glossary.png", "glossary", false),
			new Entry("gui.witchercraft.pause_menu_gui.button_bestiary", "", null, true),
			new Entry("gui.witchercraft.shell.nav.meditation", "witchercraft:textures/gui/nav/meditation.png", "meditation", false),
	};

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
		return Math.min((float) this.width / WitcherGuiLayout.DESIGN_W, (float) this.height / WitcherGuiLayout.DESIGN_H);
	}

	private float offsetX(float s) {
		return (this.width - WitcherGuiLayout.DESIGN_W * s) / 2f;
	}

	private float offsetY(float s) {
		return (this.height - WitcherGuiLayout.DESIGN_H * s) / 2f;
	}

	private static int entryX() {
		return (WitcherGuiLayout.DESIGN_W - ENTRY_W) / 2;
	}

	private static int entryY(int i) {
		int count = ENTRIES.length;
		int total = count * ENTRY_H + (count - 1) * ENTRY_GAP;
		int bandBottom = WitcherGuiLayout.NAV_Y + WitcherGuiLayout.NAV_H;
		int startY = bandBottom + (WitcherGuiLayout.DESIGN_H - bandBottom - total) / 2;
		return startY + i * (ENTRY_H + ENTRY_GAP);
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
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(WitcherGuiLayout.BG), 0, 0, 0, 0, WitcherGuiLayout.DESIGN_W, WitcherGuiLayout.DESIGN_H, WitcherGuiLayout.DESIGN_W, WitcherGuiLayout.DESIGN_H);
		g.fill(0, 0, WitcherGuiLayout.DESIGN_W, WitcherGuiLayout.DESIGN_H, PANEL_DIM);
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

		for (int i = 0; i < ENTRIES.length; i++) {
			Entry e = ENTRIES[i];
			int ex = entryX(), ey = entryY(i);
			boolean hover = dmx >= ex && dmx < ex + ENTRY_W && dmy >= ey && dmy < ey + ENTRY_H;

			g.fill(ex, ey, ex + ENTRY_W, ey + ENTRY_H, hover ? TAB_BG_ACTIVE : TAB_BG);
			g.fill(ex, ey, ex + ENTRY_W, ey + 1, TAB_BORDER);
			g.fill(ex, ey + ENTRY_H - 1, ex + ENTRY_W, ey + ENTRY_H, TAB_BORDER);
			if (hover)
				g.fill(ex, ey, ex + 2, ey + ENTRY_H, TAB_ACCENT);

			if (e.icon != null && !e.icon.isEmpty()) {
				int iy = ey + (ENTRY_H - ENTRY_ICON) / 2;
				g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(e.icon), ex + 8, iy, 0, 0, ENTRY_ICON, ENTRY_ICON, ENTRY_ICON, ENTRY_ICON);
			}

			Component label = Component.translatable(e.labelKey);
			int lw = this.font.width(label);
			g.text(this.font, label, ex + (ENTRY_W - lw) / 2, ey + (ENTRY_H - 8) / 2, hover ? TAB_TEXT_ACTIVE : TAB_TEXT, false);
		}

		g.pose().popMatrix();
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
			for (int i = 0; i < ENTRIES.length; i++) {
				int ex = entryX(), ey = entryY(i);
				if (dmx >= ex && dmx < ex + ENTRY_W && dmy >= ey && dmy < ey + ENTRY_H) {
					activate(ENTRIES[i]);
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void activate(Entry e) {
		if (e.shellPage != null) {
			// opening a screen is not server-authoritative state (see TDD 3.9)
			Minecraft.getInstance().setScreen(new WitcherGuiScreen(e.shellPage));
		} else if (e.bestiary) {
			// Bestiary has no shell page yet: open its container the old way
			ClientPacketDistributor.sendToServer(new PauseMenuGuiButtonMessage(4, x, y, z));
			PauseMenuGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
		}
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

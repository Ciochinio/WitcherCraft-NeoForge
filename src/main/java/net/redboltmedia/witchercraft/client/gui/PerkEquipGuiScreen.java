package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.PerkEquipGuiMenu;
import net.redboltmedia.witchercraft.network.PerkEquipGuiButtonMessage;
import net.redboltmedia.witchercraft.network.PerkEquipVars;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Perk equip screen - slice 1b (functional equip mechanic, placeholder art).
 *
 * HAND-MAINTAINED, locked_code=true: MCreator must not regenerate this file.
 *
 * Rendering is intentionally art-free: colored cells drawn with fill() +
 * abbreviations, so the mechanic (and mutagen colour synergy) is fully testable
 * before any glyph/frame PNGs exist. When real art lands, swap the drawCell/
 * text calls for blit() at the same PerkEquipLayout coordinates.
 *
 * Left half = debug palette of all 45 perks (interim source; the real learned-
 * gated tree replaces it in Phase 2). Right half = the equip grid.
 * Selection ("held perk") is client-only state here; only placement/removal/
 * mutagen changes go to the server (authoritative) via PerkEquipGuiButtonMessage.
 */
public class PerkEquipGuiScreen extends AbstractContainerScreen<PerkEquipGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private Button button_back;

	// Client-only selection: the perk id currently "held" (0 = nothing).
	private int heldPerk = 0;

	// Cell colours.
	private static final int PANEL_BG = 0xC00E0E12;
	private static final int DIVIDER = 0xFF2A2A30;
	private static final int CELL_EMPTY_BORDER = 0xFF4A4A52;
	private static final int CELL_EMPTY_INNER = 0xFF1B1B20;
	private static final int CELL_VALID_BORDER = 0xFFFFDD55;
	private static final int TEXT_DIM = 0xFF9A9AA2;
	private static final int TEXT_HELD = 0xFFFFFFFF;

	public PerkEquipGuiScreen(PerkEquipGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, PerkEquipLayout.PANEL_W, PerkEquipLayout.PANEL_H);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
	}

	// ---- rendering (all gui-local coords) ------------------------------------

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.fill(0, 0, PerkEquipLayout.PANEL_W, PerkEquipLayout.PANEL_H, PANEL_BG);
		g.fill(186, 0, 188, PerkEquipLayout.PANEL_H, DIVIDER);

		g.text(this.font, "PERK EQUIP", 8, 6, 0xFFFFFFFF, false);
		if (heldPerk != 0) {
			g.text(this.font, "Holding: " + PerkRegistry.name(heldPerk), 8, PerkEquipLayout.PANEL_H - 14, TEXT_HELD, false);
		} else {
			g.text(this.font, "L-click perk to hold, then a slot. L-click slot to remove.", 8, PerkEquipLayout.PANEL_H - 14, TEXT_DIM, false);
		}

		// palette rows
		for (int i = 0; i < PerkRegistry.IDS.length; i++) {
			int id = PerkRegistry.IDS[i];
			int rx = PerkEquipLayout.paletteColX(i);
			int ry = PerkEquipLayout.paletteRowY(i);
			boolean held = id == heldPerk;
			if (held)
				g.fill(rx - 1, ry - 1, rx + PerkEquipLayout.PALETTE_ROW_W, ry + PerkEquipLayout.PALETTE_ROW_H - 1, 0x66FFDD55);
			int col = held ? TEXT_HELD : PerkRegistry.tint(PerkRegistry.color(id));
			g.text(this.font, trim(PerkRegistry.NAMES[i], 15), rx, ry, col, false);
		}

		// equip grid slots
		for (int i = 0; i < PerkEquipVars.PERK_SLOTS; i++) {
			int sx = PerkEquipLayout.SLOT_X[i];
			int sy = PerkEquipLayout.SLOT_Y[i];
			int cur = PerkEquipVars.getPerkSocket(entity, i);
			boolean validTarget = heldPerk != 0 && cur == 0;
			if (cur > 0) {
				int c = PerkRegistry.tint(PerkRegistry.color(cur));
				drawCell(g, sx, sy, PerkEquipLayout.SLOT_SIZE, c, withAlpha(c, 0x55));
				g.text(this.font, trim(PerkRegistry.name(cur), 3), sx + 3, sy + PerkEquipLayout.SLOT_SIZE / 2 - 4, 0xFFFFFFFF, false);
			} else {
				drawCell(g, sx, sy, PerkEquipLayout.SLOT_SIZE, validTarget ? CELL_VALID_BORDER : CELL_EMPTY_BORDER, CELL_EMPTY_INNER);
			}
		}

		// mutagen sockets
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int sx = PerkEquipLayout.SOCKET_X[gi];
			int sy = PerkEquipLayout.SOCKET_Y[gi];
			int m = PerkEquipVars.getMutagenSocket(entity, gi);
			if (m > 0) {
				int c = PerkRegistry.tint(m); // 1/2/3 map to red/green/blue tints
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, c, withAlpha(c, 0x55));
				g.text(this.font, mutagenAbbrev(m), sx + 4, sy + PerkEquipLayout.SOCKET_SIZE / 2 - 4, 0xFFFFFFFF, false);
			} else {
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, CELL_EMPTY_BORDER, CELL_EMPTY_INNER);
				g.text(this.font, "-", sx + PerkEquipLayout.SOCKET_SIZE / 2 - 2, sy + PerkEquipLayout.SOCKET_SIZE / 2 - 4, TEXT_DIM, false);
			}
		}

		// medallion + per-group synergy counts
		drawCell(g, PerkEquipLayout.MEDALLION_X, PerkEquipLayout.MEDALLION_Y, PerkEquipLayout.MEDALLION_SIZE, 0xFF8A6D3B, 0xFF3A2E1C);
		g.text(this.font, "MED", PerkEquipLayout.MEDALLION_X + 10, PerkEquipLayout.MEDALLION_Y + 16, 0xFFEEDDBB, false);
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int matches = groupMatchCount(gi);
			int m = PerkEquipVars.getMutagenSocket(entity, gi);
			int col = m > 0 ? PerkRegistry.tint(m) : TEXT_DIM;
			g.text(this.font, "g" + (gi + 1) + ":" + matches, PerkEquipLayout.SOCKET_X[gi] + 2, PerkEquipLayout.SOCKET_Y[gi] + PerkEquipLayout.SOCKET_SIZE + 1, col, false);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		int lx = mouseX - leftPos;
		int ly = mouseY - topPos;
		int pi = hitPalette(lx, ly);
		if (pi >= 0) {
			g.setTooltipForNextFrame(font, Component.literal(PerkRegistry.NAMES[pi] + "  (id " + PerkRegistry.IDS[pi] + ")"), mouseX, mouseY);
		}
		int si = hitSlot(lx, ly);
		if (si >= 0) {
			int cur = PerkEquipVars.getPerkSocket(entity, si);
			if (cur > 0)
				g.setTooltipForNextFrame(font, Component.literal(PerkRegistry.name(cur)), mouseX, mouseY);
		}
		super.extractRenderState(g, mouseX, mouseY, partialTicks);
	}

	private void drawCell(GuiGraphicsExtractor g, int x, int y, int size, int border, int inner) {
		g.fill(x, y, x + size, y + size, border);
		g.fill(x + 1, y + 1, x + size - 1, y + size - 1, inner);
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int lx = (int) event.x() - leftPos;
		int ly = (int) event.y() - topPos;
		if (event.button() == 0) { // left: our custom regions take priority
			int pi = hitPalette(lx, ly);
			if (pi >= 0) {
				heldPerk = PerkRegistry.IDS[pi];
				return true;
			}
			int si = hitSlot(lx, ly);
			if (si >= 0) {
				int cur = PerkEquipVars.getPerkSocket(entity, si);
				if (heldPerk != 0 && cur == 0) {
					sendAction(1000000 + si * 1000 + heldPerk); // place
					heldPerk = 0;
				} else if (heldPerk == 0 && cur != 0) {
					sendAction(2000000 + si); // remove
				}
				return true;
			}
			int gi = hitSocket(lx, ly);
			if (gi >= 0) {
				sendAction(3000000 + gi); // cycle mutagen colour
				return true;
			}
		}
		// Let widgets (Back button) handle anything outside our regions.
		if (super.mouseClicked(event, doubleClick))
			return true;
		// Left-click on empty space cancels the held selection.
		if (event.button() == 0 && heldPerk != 0) {
			heldPerk = 0;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == 256) { // Esc
			if (heldPerk != 0) {
				heldPerk = 0;
				return true;
			}
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(event);
	}

	private void sendAction(int encoded) {
		ClientPacketDistributor.sendToServer(new PerkEquipGuiButtonMessage(encoded, x, y, z));
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.literal("Back"), e -> {
			this.minecraft.player.closeContainer();
		}).bounds(this.leftPos + PerkEquipLayout.PANEL_W - 50, this.topPos + PerkEquipLayout.PANEL_H - 24, 46, 20).build();
		this.addRenderableWidget(button_back);
	}

	// ---- hit tests (gui-local) ----------------------------------------------

	private int hitPalette(int lx, int ly) {
		for (int i = 0; i < PerkRegistry.IDS.length; i++) {
			int rx = PerkEquipLayout.paletteColX(i);
			int ry = PerkEquipLayout.paletteRowY(i);
			if (lx >= rx && lx < rx + PerkEquipLayout.PALETTE_ROW_W && ly >= ry && ly < ry + PerkEquipLayout.PALETTE_ROW_H)
				return i;
		}
		return -1;
	}

	private int hitSlot(int lx, int ly) {
		for (int i = 0; i < PerkEquipVars.PERK_SLOTS; i++) {
			int sx = PerkEquipLayout.SLOT_X[i];
			int sy = PerkEquipLayout.SLOT_Y[i];
			if (lx >= sx && lx < sx + PerkEquipLayout.SLOT_SIZE && ly >= sy && ly < sy + PerkEquipLayout.SLOT_SIZE)
				return i;
		}
		return -1;
	}

	private int hitSocket(int lx, int ly) {
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int sx = PerkEquipLayout.SOCKET_X[gi];
			int sy = PerkEquipLayout.SOCKET_Y[gi];
			if (lx >= sx && lx < sx + PerkEquipLayout.SOCKET_SIZE && ly >= sy && ly < sy + PerkEquipLayout.SOCKET_SIZE)
				return gi;
		}
		return -1;
	}

	// ---- helpers -------------------------------------------------------------

	/** how many perks in group gi share the socketed mutagen's colour (0-3). */
	private int groupMatchCount(int gi) {
		int m = PerkEquipVars.getMutagenSocket(entity, gi);
		if (m <= 0)
			return 0;
		int n = 0;
		for (int s = gi * 3; s < gi * 3 + 3; s++) {
			int id = PerkEquipVars.getPerkSocket(entity, s);
			if (id > 0 && PerkRegistry.color(id) == m)
				n++;
		}
		return n;
	}

	private static String mutagenAbbrev(int m) {
		switch (m) {
			case 1:
				return "RED";
			case 2:
				return "GRN";
			case 3:
				return "BLU";
			default:
				return "-";
		}
	}

	private static String trim(String s, int max) {
		return s.length() <= max ? s : s.substring(0, max);
	}

	private static int withAlpha(int argb, int alpha) {
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}
}

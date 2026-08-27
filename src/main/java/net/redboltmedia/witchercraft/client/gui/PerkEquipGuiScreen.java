package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.PerkEquipGuiMenu;
import net.redboltmedia.witchercraft.network.PerkEquipGuiButtonMessage;
import net.redboltmedia.witchercraft.network.PerkEquipVars;
import net.redboltmedia.witchercraft.network.PerkLearnedVars;
import net.redboltmedia.witchercraft.procedures.CharacterAbilitiesSkillPointsAvailableProcedure;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import java.util.List;

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
 * Left half = the perk TREE for the active branch (SLICE 2a: Combat test
 * topology from PerkTree; per-node prerequisites; right-click to learn, left-
 * click a learned node to hold for equipping). Right half = the equip grid.
 * Selection ("held perk") is client-only state here; learning, placement,
 * removal and mutagen changes all go to the server (authoritative) via
 * PerkEquipGuiButtonMessage.
 */
public class PerkEquipGuiScreen extends AbstractContainerScreen<PerkEquipGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private Button button_back;

	// Client-only selection: the perk id currently "held" (0 = nothing).
	private int heldPerk = 0;
	// Active tree branch/tab colour (1 red / 2 green / 3 blue / 4 neutral).
	// SLICE 2a: fixed to Combat; real tab switching lands next slice.
	private int activeBranch = 1;

	// Cell colours.
	private static final int PANEL_BG = 0xC00E0E12;
	private static final int DIVIDER = 0xFF2A2A30;
	private static final int CELL_EMPTY_BORDER = 0xFF4A4A52;
	private static final int CELL_EMPTY_INNER = 0xFF1B1B20;
	private static final int CELL_VALID_BORDER = 0xFFFFDD55;
	private static final int TEXT_DIM = 0xFF9A9AA2;
	private static final int TEXT_HELD = 0xFFFFFFFF;

	// Branch tabs (index i -> branch colour i+1: Combat/Alchemy/Signs/General).
	private static final String[] TAB_LABELS = {"CMB", "ALC", "SGN", "GEN"};
	private static final int[] TAB_X = {8, 52, 96, 140};
	private static final int TAB_Y = 6, TAB_W = 40, TAB_H = 11;

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

		drawTabs(g);
		g.text(this.font, "Pts:", 8, PerkEquipLayout.PANEL_H - 14, 0xFFDDDD88, false);
		g.text(this.font, CharacterAbilitiesSkillPointsAvailableProcedure.execute(entity), 30, PerkEquipLayout.PANEL_H - 14, 0xFFDDDD88, false);
		if (heldPerk != 0) {
			g.text(this.font, "Holding: " + PerkRegistry.name(heldPerk), 60, PerkEquipLayout.PANEL_H - 14, TEXT_HELD, false);
		} else {
			g.text(this.font, "R-click=learn  L-click learned=hold", 60, PerkEquipLayout.PANEL_H - 14, TEXT_DIM, false);
		}

		// left panel: the perk tree for the active branch
		drawTree(g);

		// mutagen connector lines (under the cells): each socketed mutagen links
		// to the equipped perks in its group that MATCH its colour, in that colour.
		drawConnectors(g);

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

		// medallion (optional, resizable rectangle) + per-group synergy counts
		if (PerkEquipLayout.MEDALLION_ENABLED) {
			drawRect(g, PerkEquipLayout.MEDALLION_X, PerkEquipLayout.MEDALLION_Y, PerkEquipLayout.MEDALLION_W, PerkEquipLayout.MEDALLION_H, 0xFF8A6D3B, 0xFF3A2E1C);
			g.text(this.font, "MED", PerkEquipLayout.MEDALLION_X + PerkEquipLayout.MEDALLION_W / 2 - 8, PerkEquipLayout.MEDALLION_Y + PerkEquipLayout.MEDALLION_H / 2 - 4, 0xFFEEDDBB, false);
		}
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
		int np = hitNode(lx, ly);
		if (np > 0) {
			PerkTree.Node n = PerkTree.byId(np);
			String state = PerkLearnedVars.isLearned(entity, np) ? "Learned"
					: (n != null && prereqsMet(n) ? "Available - right-click to learn" : "Locked");
			g.setTooltipForNextFrame(font, Component.literal(PerkRegistry.name(np) + " - " + state), mouseX, mouseY);
		}
		int si = hitSlot(lx, ly);
		if (si >= 0) {
			int cur = PerkEquipVars.getPerkSocket(entity, si);
			if (cur > 0)
				g.setTooltipForNextFrame(font, Component.literal(PerkRegistry.name(cur)), mouseX, mouseY);
		}
		super.extractRenderState(g, mouseX, mouseY, partialTicks);
	}

	// ---- left panel: perk tree ----------------------------------------------

	private void drawTabs(GuiGraphicsExtractor g) {
		for (int i = 0; i < TAB_LABELS.length; i++) {
			boolean active = activeBranch == i + 1;
			g.text(this.font, TAB_LABELS[i], TAB_X[i], TAB_Y, active ? 0xFFFFFFFF : TEXT_DIM, false);
			if (active)
				g.fill(TAB_X[i] - 1, TAB_Y + 9, TAB_X[i] + 22, TAB_Y + 10, PerkRegistry.tint(i + 1));
		}
	}

	private int hitTab(int lx, int ly) {
		for (int i = 0; i < TAB_LABELS.length; i++) {
			if (lx >= TAB_X[i] - 2 && lx < TAB_X[i] + TAB_W && ly >= TAB_Y - 2 && ly < TAB_Y + TAB_H)
				return i + 1;
		}
		return -1;
	}

	private void drawTree(GuiGraphicsExtractor g) {
		List<PerkTree.Node> nodes = PerkTree.forColor(activeBranch);
		int tint = PerkRegistry.tint(activeBranch);
		// prerequisite connectors first (drawn under the nodes)
		for (PerkTree.Node n : nodes) {
			boolean childLearned = PerkLearnedVars.isLearned(entity, n.perkId);
			boolean childMet = prereqsMet(n);
			int col = childLearned ? withAlpha(tint, 0xCC) : (childMet ? withAlpha(tint, 0x77) : 0xFF3A3A42);
			for (int pre : n.prereqs) {
				PerkTree.Node p = PerkTree.byId(pre);
				if (p == null)
					continue;
				hLine(g, p.cx(), n.cx(), p.cy(), col); // horizontal at prereq's y
				vLine(g, n.cx(), p.cy(), n.cy(), col); // vertical at child's x
			}
		}
		// nodes
		for (PerkTree.Node n : nodes) {
			boolean learned = PerkLearnedVars.isLearned(entity, n.perkId);
			boolean equipped = learned && PerkEquipVars.isPerkSocketed(entity, n.perkId);
			boolean met = prereqsMet(n);
			int t = PerkRegistry.tint(PerkRegistry.color(n.perkId));
			if (heldPerk == n.perkId) // held-for-equip highlight ring
				drawRect(g, n.x - 2, n.y - 2, PerkTree.NODE_SIZE + 4, PerkTree.NODE_SIZE + 4, CELL_VALID_BORDER, CELL_VALID_BORDER);
			int border, inner, textCol;
			if (equipped) { // fully bright, filled
				border = t;
				inner = withAlpha(t, 0x99);
				textCol = 0xFFFFFFFF;
			} else if (learned) { // learned but not equipped: bright rim, dark inner
				border = t;
				inner = CELL_EMPTY_INNER;
				textCol = t;
			} else if (met) { // available to learn: dark with a faint colour rim
				border = dim(t);
				inner = 0xFF141417;
				textCol = dim(t);
			} else { // locked (prereqs unmet): fully dark/flat
				border = 0xFF2A2A30;
				inner = 0xFF121215;
				textCol = 0xFF44444E;
			}
			drawCell(g, n.x, n.y, PerkTree.NODE_SIZE, border, inner);
			g.text(this.font, trim(PerkRegistry.name(n.perkId), 3), n.x + 3, n.y + PerkTree.NODE_SIZE / 2 - 4, textCol, false);
		}
	}

	private boolean prereqsMet(PerkTree.Node n) {
		for (int pre : n.prereqs)
			if (!PerkLearnedVars.isLearned(entity, pre))
				return false;
		return true;
	}

	private void drawConnectors(GuiGraphicsExtractor g) {
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int m = PerkEquipVars.getMutagenSocket(entity, gi);
			if (m <= 0)
				continue;
			int col = withAlpha(PerkRegistry.tint(m), 0xCC);
			int sox = PerkEquipLayout.SOCKET_X[gi];
			int scx = sox + PerkEquipLayout.SOCKET_SIZE / 2;
			int scy = PerkEquipLayout.SOCKET_Y[gi] + PerkEquipLayout.SOCKET_SIZE / 2;

			// first pass: collect the matching equipped perks (correct colour),
			// their average x (to place the trunk) and the y-span of the bus.
			int matchCount = 0, sumCenterX = 0, yMin = scy, yMax = scy;
			for (int s = gi * 3; s < gi * 3 + 3; s++) {
				int id = PerkEquipVars.getPerkSocket(entity, s);
				if (id <= 0 || PerkRegistry.color(id) != m)
					continue;
				int slcx = PerkEquipLayout.SLOT_X[s] + PerkEquipLayout.SLOT_SIZE / 2;
				int slcy = PerkEquipLayout.SLOT_Y[s] + PerkEquipLayout.SLOT_SIZE / 2;
				sumCenterX += slcx;
				matchCount++;
				yMin = Math.min(yMin, slcy);
				yMax = Math.max(yMax, slcy);
			}
			if (matchCount == 0)
				continue;

			// trunk sits in the gap between the socket and the slot column, so it
			// never overlaps intermediate boxes; stubs meet each box at its edge.
			int trunkX = (scx + sumCenterX / matchCount) / 2;
			vLine(g, trunkX, yMin, yMax, col);
			hLine(g, edgeTowards(sox, PerkEquipLayout.SOCKET_SIZE, trunkX), trunkX, scy, col);
			for (int s = gi * 3; s < gi * 3 + 3; s++) {
				int id = PerkEquipVars.getPerkSocket(entity, s);
				if (id <= 0 || PerkRegistry.color(id) != m)
					continue;
				int slx = PerkEquipLayout.SLOT_X[s];
				int slcy = PerkEquipLayout.SLOT_Y[s] + PerkEquipLayout.SLOT_SIZE / 2;
				hLine(g, edgeTowards(slx, PerkEquipLayout.SLOT_SIZE, trunkX), trunkX, slcy, col);
			}
		}
	}

	/** x of the box edge facing the trunk (right edge if the box is left of it). */
	private int edgeTowards(int boxX, int boxSize, int trunkX) {
		return (boxX + boxSize / 2) < trunkX ? boxX + boxSize : boxX;
	}

	private void hLine(GuiGraphicsExtractor g, int xa, int xb, int y, int col) {
		g.fill(Math.min(xa, xb), y - 1, Math.max(xa, xb), y + 1, col);
	}

	private void vLine(GuiGraphicsExtractor g, int x, int ya, int yb, int col) {
		g.fill(x - 1, Math.min(ya, yb), x + 1, Math.max(ya, yb), col);
	}

	private void drawCell(GuiGraphicsExtractor g, int x, int y, int size, int border, int inner) {
		drawRect(g, x, y, size, size, border, inner);
	}

	private void drawRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int border, int inner) {
		g.fill(x, y, x + w, y + h, border);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, inner);
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int lx = (int) event.x() - leftPos;
		int ly = (int) event.y() - topPos;
		int nodePerk = hitNode(lx, ly);
		if (event.button() == 1) { // right-click a node = learn (server enforces prereqs + points)
			if (nodePerk > 0) {
				if (!PerkLearnedVars.isLearned(entity, nodePerk))
					sendAction(5000000 + nodePerk);
				return true;
			}
			return super.mouseClicked(event, doubleClick);
		}
		if (event.button() == 0) { // left: our custom regions take priority
			int tab = hitTab(lx, ly);
			if (tab > 0) {
				activeBranch = tab;
				return true;
			}
			if (nodePerk > 0) {
				if (PerkLearnedVars.isLearned(entity, nodePerk))
					heldPerk = nodePerk; // hold a learned perk for equipping
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
			if (hitMedallion(lx, ly)) {
				sendAction(4000000); // medallion click (placeholder)
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

	/** perk id of the tree node under (lx,ly) in the active branch, or -1. */
	private int hitNode(int lx, int ly) {
		for (PerkTree.Node n : PerkTree.forColor(activeBranch)) {
			if (lx >= n.x && lx < n.x + PerkTree.NODE_SIZE && ly >= n.y && ly < n.y + PerkTree.NODE_SIZE)
				return n.perkId;
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

	private boolean hitMedallion(int lx, int ly) {
		if (!PerkEquipLayout.MEDALLION_ENABLED)
			return false;
		return lx >= PerkEquipLayout.MEDALLION_X && lx < PerkEquipLayout.MEDALLION_X + PerkEquipLayout.MEDALLION_W
				&& ly >= PerkEquipLayout.MEDALLION_Y && ly < PerkEquipLayout.MEDALLION_Y + PerkEquipLayout.MEDALLION_H;
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

	/** darken an opaque colour toward black (used to grey out equipped perks). */
	private static int dim(int argb) {
		int r = (argb >> 16 & 0xFF) * 42 / 100;
		int g = (argb >> 8 & 0xFF) * 42 / 100;
		int b = (argb & 0xFF) * 42 / 100;
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}
}

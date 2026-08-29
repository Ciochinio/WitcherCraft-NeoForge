package net.redboltmedia.witchercraft;

import java.util.List;

import net.redboltmedia.witchercraft.procedures.CharacterAbilitiesSkillPointsAvailableProcedure;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * The perk tree + equip grid, ported from the retired PerkEquipGuiScreen into a
 * {@link GuiPage} inside {@link WitcherGuiScreen}.
 *
 * Behaviour is unchanged: left half = the perk TREE for the active branch
 * (right-click to learn, left-click a learned node to hold), right half = the
 * equip grid + mutagen sockets. All authoritative changes still go to the
 * server via {@link PerkEquipGuiButtonMessage}; geometry still comes from the
 * tool-generated {@link PerkEquipLayout} + {@link PerkTree}, so the existing
 * tools/equip-grid-placer.html and tools/tree-node-placer.html keep working.
 *
 * The only structural change vs the old container screen: it draws through
 * origin-offset helpers (the shell hands it a content origin instead of the old
 * leftPos / topPos), and the perk recompute that used to fire from
 * PerkEquipGuiMenu.removed() now runs server-side after each equip change (see
 * PerkEquipGuiButtonMessage).
 */
public class PerkPage implements GuiPage {

	// Cell colours (from the old screen).
	private static final int CELL_EMPTY_BORDER = 0xFF4A4A52;
	private static final int CELL_EMPTY_INNER = 0xFF1B1B20;
	private static final int CELL_VALID_BORDER = 0xFFFFDD55;
	private static final int TEXT_DIM = 0xFF9A9AA2;
	private static final int TEXT_HELD = 0xFFFFFFFF;

	// Branch tabs (index i -> branch colour i+1: Combat/Alchemy/Signs/General).
	private static final String[] TAB_KEYS = {
			"gui.witchercraft.shell.skills.tab_combat", "gui.witchercraft.shell.skills.tab_alchemy",
			"gui.witchercraft.shell.skills.tab_signs", "gui.witchercraft.shell.skills.tab_general"};
	private static final String[] TAB_FALLBACKS = {"CMB", "ALC", "SGN", "GEN"};
	private static final int[] TAB_X = {8, 52, 96, 140};
	private static final int TAB_Y = 6, TAB_W = 40, TAB_H = 11;

	private final String pageId;

	// Client-only selection: the perk id currently "held" (0 = nothing).
	private int heldPerk = 0;
	// Active tree branch/tab colour (1 red / 2 green / 3 blue / 4 neutral).
	private int activeBranch = 1;

	// Per-frame context (set at the top of render); helpers offset by (ox, oy).
	private int ox, oy;
	private Font font;
	// Tooltip lines computed during render, rendered by the shell in screen space.
	private List<Component> pendingTooltip;

	public PerkPage(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public String id() {
		return pageId;
	}

	@Override
	public Component navLabel() {
		return Component.translatable("gui.witchercraft.shell.nav.skills");
	}

	private static Player player() {
		return Minecraft.getInstance().player;
	}

	// ---- rendering (gui-local coords, offset by ox/oy in the helpers) --------

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		this.font = Minecraft.getInstance().font;
		this.pendingTooltip = null;
		Player entity = player();
		if (entity == null)
			return;

		// map the 360x230 perk layout onto the content region (uniform scale,
		// centred) so the Skills page fills the area below the navbar. Drawing is
		// in perk-local coords via a nested pose transform (ox/oy stay 0).
		float scale = fitScale(w, h);
		float px = x + (w - PerkEquipLayout.PANEL_W * scale) / 2f;
		float py = y + (h - PerkEquipLayout.PANEL_H * scale) / 2f;
		this.ox = 0;
		this.oy = 0;
		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(scale, scale);

		drawTabs(g);
		// status row: points-available text is not yet localized (a legacy
		// procedure shared with 3 retired GUIs - see TDD 3.11), so its width is
		// measured rather than assumed, and the tail text is positioned after it
		// to avoid the two overlapping regardless of locale/points-string length.
		String pts = CharacterAbilitiesSkillPointsAvailableProcedure.execute(entity);
		int statusY = PerkEquipLayout.PANEL_H - 14;
		text(g, pts, 8, statusY, 0xFFDDDD88);
		int tailX = 8 + font.width(pts) + 10;
		// no separate "Holding: X" readout - the node's selection ring and the
		// lit-up valid-target slots already show what's held and where it can go.
		textC(g, tt("gui.witchercraft.shell.skills.instructions", "R-click=learn  L-click learned=hold"), tailX, statusY, TEXT_DIM);

		drawTree(g, entity);
		drawConnectors(g, entity);

		// equip grid slots
		for (int i = 0; i < PerkEquipVars.PERK_SLOTS; i++) {
			int sx = PerkEquipLayout.SLOT_X[i];
			int sy = PerkEquipLayout.SLOT_Y[i];
			int cur = PerkEquipVars.getPerkSocket(entity, i);
			// while holding, EVERY slot is a legal target now (empty = place,
			// occupied = swap, evicting the occupant back to the pool).
			boolean validTarget = heldPerk != 0;
			if (cur > 0) {
				int c = PerkRegistry.tint(PerkRegistry.color(cur));
				drawCell(g, sx, sy, PerkEquipLayout.SLOT_SIZE, validTarget ? CELL_VALID_BORDER : c, withAlpha(c, 0x55));
				drawPerkIcon(g, cur, sx, sy, PerkEquipLayout.SLOT_SIZE, ICON_EQUIPPED); // socketed = equipped
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
				int c = PerkRegistry.tint(m);
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, c, withAlpha(c, 0x55));
				textC(g, tt(mutagenAbbrevKey(m), mutagenAbbrevFallback(m)), sx + 4, sy + PerkEquipLayout.SOCKET_SIZE / 2 - 4, 0xFFFFFFFF);
			} else {
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, CELL_EMPTY_BORDER, CELL_EMPTY_INNER);
				text(g, "-", sx + PerkEquipLayout.SOCKET_SIZE / 2 - 2, sy + PerkEquipLayout.SOCKET_SIZE / 2 - 4, TEXT_DIM);
			}
		}

		// medallion + per-group synergy counts
		if (PerkEquipLayout.MEDALLION_ENABLED) {
			drawRect(g, PerkEquipLayout.MEDALLION_X, PerkEquipLayout.MEDALLION_Y, PerkEquipLayout.MEDALLION_W, PerkEquipLayout.MEDALLION_H, 0xFF8A6D3B, 0xFF3A2E1C);
			textC(g, tt("gui.witchercraft.shell.skills.medallion", "MED"), PerkEquipLayout.MEDALLION_X + PerkEquipLayout.MEDALLION_W / 2 - 8, PerkEquipLayout.MEDALLION_Y + PerkEquipLayout.MEDALLION_H / 2 - 4, 0xFFEEDDBB);
		}
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int matches = groupMatchCount(gi);
			int m = PerkEquipVars.getMutagenSocket(entity, gi);
			int col = m > 0 ? PerkRegistry.tint(m) : TEXT_DIM;
			text(g, "g" + (gi + 1) + ":" + matches, PerkEquipLayout.SOCKET_X[gi] + 2, PerkEquipLayout.SOCKET_Y[gi] + PerkEquipLayout.SOCKET_SIZE + 1, col);
		}

		g.pose().popMatrix();

		// tooltips - stored, not drawn here (the shell renders them in screen space).
		// Hit-test in perk-local coords (map the mouse back through the fit scale).
		int lx = (int) ((mouseX - px) / scale), ly = (int) ((mouseY - py) / scale);
		int np = hitNode(lx, ly);
		if (np > 0) {
			pendingTooltip = perkTooltip(np);
		} else {
			int si = hitSlot(lx, ly);
			if (si >= 0) {
				int cur = PerkEquipVars.getPerkSocket(entity, si);
				if (cur > 0)
					pendingTooltip = perkTooltip(cur);
			}
		}
	}

	@Override
	public List<Component> pollTooltip() {
		return pendingTooltip;
	}

	// Tooltip pixel width the description wraps to. The tooltip is drawn in
	// SCREEN space (after the shell's pose scale is popped - see render()), so
	// this is measured against the real, unscaled font, same as it will render.
	private static final int TOOLTIP_WRAP_WIDTH = 200;

	// name in its branch colour, then the description wrapped onto as many lines
	// as it needs - the learned/available/locked state is dropped here since the
	// icon art (3 glyph states) and the cell border colour already convey it.
	private List<Component> perkTooltip(int perkId) {
		int rgb = PerkRegistry.tint(PerkRegistry.color(perkId)) & 0xFFFFFF;
		String fallback = PerkRegistry.fallbackName(perkId);
		// translatableWithFallback: if the lang key is ever missing (see
		// PerkRegistry.fallbackName), shows readable placeholder text instead of
		// the raw dotted key.
		Component name = Component.translatableWithFallback(PerkRegistry.nameKey(perkId), fallback)
				.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
		Component desc = Component.translatableWithFallback(PerkRegistry.descKey(perkId), fallback + ": Placeholder description.");
		List<Component> lines = new ArrayList<>(3);
		lines.add(name);
		lines.addAll(wrapToWidth(desc, TOOLTIP_WRAP_WIDTH));
		return lines;
	}

	// Greedy word-wrap: Minecraft's List<Component> tooltip renders one line per
	// entry with no wrapping of its own, so a description longer than a couple of
	// words needs to be pre-split. Wraps on the resolved (localized) plain text,
	// not the Component's formatting, since none of these descriptions carry
	// inline styling worth preserving per-word.
	private List<Component> wrapToWidth(Component c, int maxWidth) {
		String text = c.getString();
		List<Component> out = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			String candidate = line.length() == 0 ? word : line + " " + word;
			if (font.width(candidate) > maxWidth && line.length() > 0) {
				out.add(Component.literal(line.toString()));
				line = new StringBuilder(word);
			} else {
				line = new StringBuilder(candidate);
			}
		}
		if (line.length() > 0)
			out.add(Component.literal(line.toString()));
		return out;
	}

	// ---- offset-aware primitives --------------------------------------------

	private void fill(GuiGraphicsExtractor g, int x, int y, int x2, int y2, int col) {
		g.fill(ox + x, oy + y, ox + x2, oy + y2, col);
	}

	private void text(GuiGraphicsExtractor g, String s, int x, int y, int col) {
		g.text(font, Component.literal(s), ox + x, oy + y, col, false);
	}

	private void textC(GuiGraphicsExtractor g, Component c, int x, int y, int col) {
		g.text(font, c, ox + x, oy + y, col, false);
	}

	// shorthand for a translatable with a readable fallback (see PerkRegistry.fallbackName).
	private static Component tt(String key, String fallback) {
		return Component.translatableWithFallback(key, fallback);
	}

	// ---- perk icons (32x32 source, scaled to the target cell) ----------------

	// Each perk has its own folder textures/screens/perk/<slug>/ with three state
	// glyphs. The coloured frame / selection highlight is drawn by the GUI, not
	// baked into the glyph, so these are bare icons.
	private static final int ICON_NOTLEARNED = 0, ICON_NOTEQUIPPED = 1, ICON_EQUIPPED = 2;
	private static final String[] ICON_STATE = {"notlearned", "notequipped", "equipped"};

	// Source glyphs are 32x32; cells are 24-27px, so each icon draws inside a
	// nested pose scaled to the cell.
	private static final int ICON_SRC = 32;
	private static final Map<String, Identifier> ICON_CACHE = new HashMap<>();

	private static Identifier perkIcon(int perkId, int state) {
		String slug = PerkRegistry.slug(perkId);
		if (slug.isEmpty())
			return null;
		String key = slug + "/" + ICON_STATE[state];
		return ICON_CACHE.computeIfAbsent(key, k -> Identifier.parse("witchercraft:textures/screens/perk/" + k + ".png"));
	}

	/** Draw a perk's glyph filling a size x size cell at (x, y). Falls back to
	 *  the 3-letter label if the texture id can't be built (unknown perk). */
	private void drawPerkIcon(GuiGraphicsExtractor g, int perkId, int x, int y, int size, int state) {
		Identifier id = perkIcon(perkId, state);
		if (id == null) {
			text(g, trim(PerkRegistry.name(perkId), 3), x + 3, y + size / 2 - 4, state == ICON_NOTLEARNED ? TEXT_DIM : TEXT_HELD);
			return;
		}
		float s = size / (float) ICON_SRC;
		g.pose().pushMatrix();
		g.pose().translate((float) (ox + x), (float) (oy + y));
		g.pose().scale(s, s);
		g.blit(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0, 0, ICON_SRC, ICON_SRC, ICON_SRC, ICON_SRC);
		g.pose().popMatrix();
	}

	// ---- left panel: perk tree ----------------------------------------------

	private void drawTabs(GuiGraphicsExtractor g) {
		for (int i = 0; i < TAB_KEYS.length; i++) {
			boolean active = activeBranch == i + 1;
			textC(g, tt(TAB_KEYS[i], TAB_FALLBACKS[i]), TAB_X[i], TAB_Y, active ? 0xFFFFFFFF : TEXT_DIM);
			if (active)
				fill(g, TAB_X[i] - 1, TAB_Y + 9, TAB_X[i] + 22, TAB_Y + 10, PerkRegistry.tint(i + 1));
		}
	}

	private int hitTab(int lx, int ly) {
		for (int i = 0; i < TAB_KEYS.length; i++) {
			if (lx >= TAB_X[i] - 2 && lx < TAB_X[i] + TAB_W && ly >= TAB_Y - 2 && ly < TAB_Y + TAB_H)
				return i + 1;
		}
		return -1;
	}

	private void drawTree(GuiGraphicsExtractor g, Player entity) {
		List<PerkTree.Node> nodes = PerkTree.forColor(activeBranch);
		int tint = PerkRegistry.tint(activeBranch);
		for (PerkTree.Node n : nodes) {
			boolean childLearned = PerkLearnedVars.isLearned(entity, n.perkId);
			boolean childMet = prereqsMet(n, entity);
			int col = childLearned ? withAlpha(tint, 0xCC) : (childMet ? withAlpha(tint, 0x77) : 0xFF3A3A42);
			for (int pre : n.prereqs) {
				PerkTree.Node p = PerkTree.byId(pre);
				if (p == null)
					continue;
				hLine(g, p.cx(), n.cx(), p.cy(), col);
				vLine(g, n.cx(), p.cy(), n.cy(), col);
			}
		}
		for (PerkTree.Node n : nodes) {
			boolean learned = PerkLearnedVars.isLearned(entity, n.perkId);
			boolean equipped = learned && PerkEquipVars.isPerkSocketed(entity, n.perkId);
			boolean met = prereqsMet(n, entity);
			int t = PerkRegistry.tint(PerkRegistry.color(n.perkId));
			if (heldPerk == n.perkId) // 1px selection ring (matches the slot frames)
				drawRect(g, n.x - 1, n.y - 1, PerkTree.NODE_SIZE + 2, PerkTree.NODE_SIZE + 2, CELL_VALID_BORDER, CELL_VALID_BORDER);
			int border, inner;
			if (equipped) {
				border = t;
				inner = withAlpha(t, 0x99);
			} else if (learned) {
				border = t;
				inner = CELL_EMPTY_INNER;
			} else if (met) {
				border = dim(t);
				inner = 0xFF141417;
			} else {
				border = 0xFF2A2A30;
				inner = 0xFF121215;
			}
			drawCell(g, n.x, n.y, PerkTree.NODE_SIZE, border, inner);
			// three-state glyph: locked/available -> notlearned, learned but not
			// slotted -> notequipped, slotted -> equipped.
			int state = equipped ? ICON_EQUIPPED : (learned ? ICON_NOTEQUIPPED : ICON_NOTLEARNED);
			drawPerkIcon(g, n.perkId, n.x, n.y, PerkTree.NODE_SIZE, state);
		}
	}

	// A node's prereqs are an OR group: zero prereqs = always available, one or
	// more = any single one being learned satisfies the node (several parents are
	// alternative unlock paths, not a converging AND requirement). Mirrors the
	// server-side check in PerkEquipGuiButtonMessage - see TDD 3.10.
	private boolean prereqsMet(PerkTree.Node n, Player entity) {
		if (n.prereqs.length == 0)
			return true;
		for (int pre : n.prereqs)
			if (PerkLearnedVars.isLearned(entity, pre))
				return true;
		return false;
	}

	private void drawConnectors(GuiGraphicsExtractor g, Player entity) {
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int m = PerkEquipVars.getMutagenSocket(entity, gi);
			if (m <= 0)
				continue;
			int col = withAlpha(PerkRegistry.tint(m), 0xCC);
			int sox = PerkEquipLayout.SOCKET_X[gi];
			int scx = sox + PerkEquipLayout.SOCKET_SIZE / 2;
			int scy = PerkEquipLayout.SOCKET_Y[gi] + PerkEquipLayout.SOCKET_SIZE / 2;

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

	private int edgeTowards(int boxX, int boxSize, int trunkX) {
		return (boxX + boxSize / 2) < trunkX ? boxX + boxSize : boxX;
	}

	private void hLine(GuiGraphicsExtractor g, int xa, int xb, int y, int col) {
		fill(g, Math.min(xa, xb), y - 1, Math.max(xa, xb), y + 1, col);
	}

	private void vLine(GuiGraphicsExtractor g, int x, int ya, int yb, int col) {
		fill(g, x - 1, Math.min(ya, yb), x + 1, Math.max(ya, yb), col);
	}

	private void drawCell(GuiGraphicsExtractor g, int x, int y, int size, int border, int inner) {
		drawRect(g, x, y, size, size, border, inner);
	}

	private void drawRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int border, int inner) {
		fill(g, x, y, x + w, y + h, border);
		fill(g, x + 1, y + 1, x + w - 1, y + h - 1, inner);
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		Player entity = player();
		if (entity == null)
			return false;
		float scale = fitScale(w, h);
		float px = x + (w - PerkEquipLayout.PANEL_W * scale) / 2f;
		float py = y + (h - PerkEquipLayout.PANEL_H * scale) / 2f;
		int lx = (int) ((mouseX - px) / scale);
		int ly = (int) ((mouseY - py) / scale);
		int nodePerk = hitNode(lx, ly);
		if (button == 1) { // right-click a node = learn (server enforces prereqs + points)
			if (nodePerk > 0) {
				if (!PerkLearnedVars.isLearned(entity, nodePerk))
					sendAction(5000000 + nodePerk, entity);
				return true;
			}
			return false;
		}
		if (button == 0) {
			int tab = hitTab(lx, ly);
			if (tab > 0) {
				activeBranch = tab;
				return true;
			}
			if (nodePerk > 0) {
				if (PerkLearnedVars.isLearned(entity, nodePerk))
					heldPerk = nodePerk;
				return true;
			}
			int si = hitSlot(lx, ly);
			if (si >= 0) {
				int cur = PerkEquipVars.getPerkSocket(entity, si);
				if (heldPerk != 0) {
					// place, or swap: an occupied slot's perk returns to the pool
					sendAction(1000000 + si * 1000 + heldPerk, entity);
					heldPerk = 0;
				} else if (cur != 0) {
					sendAction(2000000 + si, entity); // remove
				}
				return true;
			}
			int gi = hitSocket(lx, ly);
			if (gi >= 0) {
				sendAction(3000000 + gi, entity); // cycle mutagen colour
				return true;
			}
			if (hitMedallion(lx, ly)) {
				sendAction(4000000, entity); // medallion click (placeholder)
				return true;
			}
			// left-click on empty space cancels the held selection
			if (heldPerk != 0) {
				heldPerk = 0;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode) {
		if (keyCode == 256 && heldPerk != 0) { // Esc clears the held perk before closing
			heldPerk = 0;
			return true;
		}
		return false;
	}

	private void sendAction(int encoded, Player entity) {
		ClientPacketDistributor.sendToServer(new PerkEquipGuiButtonMessage(encoded, (int) entity.getX(), (int) entity.getY(), (int) entity.getZ()));
	}

	// ---- hit tests (gui-local) ----------------------------------------------

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

	/** Uniform scale to fit the 360x230 perk layout into a w x h content region. */
	private static float fitScale(int w, int h) {
		return Math.min((float) w / PerkEquipLayout.PANEL_W, (float) h / PerkEquipLayout.PANEL_H);
	}

	private int groupMatchCount(int gi) {
		Player entity = player();
		if (entity == null)
			return 0;
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

	private static String mutagenAbbrevKey(int m) {
		switch (m) {
			case 1:
				return "gui.witchercraft.shell.skills.mutagen_red";
			case 2:
				return "gui.witchercraft.shell.skills.mutagen_green";
			case 3:
				return "gui.witchercraft.shell.skills.mutagen_blue";
			default:
				return "gui.witchercraft.shell.skills.mutagen_empty";
		}
	}

	private static String mutagenAbbrevFallback(int m) {
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

	private static int dim(int argb) {
		int r = (argb >> 16 & 0xFF) * 42 / 100;
		int g = (argb >> 8 & 0xFF) * 42 / 100;
		int b = (argb & 0xFF) * 42 / 100;
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}
}

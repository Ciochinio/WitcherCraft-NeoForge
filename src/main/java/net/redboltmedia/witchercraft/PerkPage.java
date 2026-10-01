package net.redboltmedia.witchercraft;

import java.util.List;

import net.redboltmedia.witchercraft.procedures.CharacterAbilitiesSkillPointsAvailableProcedure;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
 * Drawn by {@link SkillsScreen} (the Skills tab is container-backed): the four
 * mutagen sockets are real slots there, so this page only draws their frames
 * and reads their items through {@link #setMutagenSource}. The fifth sub-tab,
 * Mutagens, hides the tree so the screen can show the player inventory in its
 * place ({@link #mutagensShown}).
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

	// Sub-tabs (index i -> activeBranch i+1): the four perk branches
	// (Combat/Alchemy/Signs/General = branch colours 1-4), then Mutagens, which
	// shows the player inventory instead of a tree. Icons are placeholders drawn
	// from vanilla item textures; swap a path to reskin a tab.
	public static final int TAB_MUTAGENS = 5;
	private static final String[] TAB_KEYS = {
			"gui.witchercraft.shell.skills.tab_combat", "gui.witchercraft.shell.skills.tab_alchemy",
			"gui.witchercraft.shell.skills.tab_signs", "gui.witchercraft.shell.skills.tab_general",
			"gui.witchercraft.shell.skills.tab_mutagens"};
	private static final String[] TAB_FALLBACKS = {"Combat", "Alchemy", "Signs", "General", "Mutagens"};
	private static final String[] TAB_ICONS = {
			"minecraft:textures/item/iron_sword.png", "minecraft:textures/item/brewing_stand.png",
			"minecraft:textures/item/lapis_lazuli.png", "minecraft:textures/item/cooked_beef.png",
			"minecraft:textures/item/redstone.png"};
	private static final int MUTAGENS_TAB_ACCENT = 0xFFB03030;
	private static final int[] TAB_X = {8, 40, 72, 104, 136};
	private static final int TAB_Y = 4, TAB_ICON = 12;
	private static final int TAB_DIM = 0x99000000; // darkens the inactive tab icons

	// The area the Mutagens tab gives the player inventory (perk-local coords):
	// the tree's half of the panel, between the tab row and the status row.
	public static final int INVENTORY_AREA_X = 0, INVENTORY_AREA_Y = 20;
	public static final int INVENTORY_AREA_W = 190, INVENTORY_AREA_H = PerkEquipLayout.PANEL_H - 20 - 16;

	private final String pageId;

	// Client-only selection: the perk id currently "held" (0 = nothing).
	private int heldPerk = 0;
	// Active sub-tab: tree branch colour (1 red / 2 green / 3 blue / 4 neutral),
	// or TAB_MUTAGENS.
	private int activeBranch = 1;
	// The item in each mutagen socket. SkillsScreen points this at its menu's
	// socket slots (the sockets themselves are real slots it draws over).
	private IntFunction<ItemStack> mutagens = gi -> ItemStack.EMPTY;

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

	/** Where the mutagen socket contents come from (SkillsScreen's menu). */
	public void setMutagenSource(IntFunction<ItemStack> source) {
		this.mutagens = source;
	}

	/** The Mutagens sub-tab is active: the inventory replaces the tree. */
	public boolean mutagensShown() {
		return activeBranch == TAB_MUTAGENS;
	}

	/**
	 * Where the 360x230 perk layout lands in a w x h region (design coords), as
	 * {originX, originY, scale}: uniform fit, centred. Perk-local (lx, ly) maps to
	 * (originX + lx * scale, originY + ly * scale).
	 */
	public static float[] layout(int x, int y, int w, int h) {
		float scale = fitScale(w, h);
		return new float[]{x + (w - PerkEquipLayout.PANEL_W * scale) / 2f, y + (h - PerkEquipLayout.PANEL_H * scale) / 2f, scale};
	}

	private MutagenEffects.Type mutagenType(int gi) {
		return MutagenEffects.typeOf(mutagens.apply(gi));
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
		float[] fit = layout(x, y, w, h);
		float px = fit[0], py = fit[1], scale = fit[2];
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
		if (mutagensShown())
			textC(g, tt("gui.witchercraft.shell.skills.mutagen_instructions", "Drag a mutagen into a socket"), tailX, statusY, TEXT_DIM);
		else
			textC(g, tt("gui.witchercraft.shell.skills.instructions", "R-click=learn  L-click learned=hold"), tailX, statusY, TEXT_DIM);

		// the Mutagens tab leaves the tree area to the inventory slots SkillsScreen draws
		if (!mutagensShown())
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

		// mutagen sockets: the frame, tinted by the socketed mutagen's type. The
		// mutagen item itself is a real slot SkillsScreen centres in the frame.
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int sx = PerkEquipLayout.SOCKET_X[gi];
			int sy = PerkEquipLayout.SOCKET_Y[gi];
			MutagenEffects.Type type = mutagenType(gi);
			if (type != null) {
				int c = type.tint();
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, c, withAlpha(c, 0x55));
			} else {
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, CELL_EMPTY_BORDER, CELL_EMPTY_INNER);
			}
		}

		// medallion + per-group synergy counts
		if (PerkEquipLayout.MEDALLION_ENABLED) {
			drawRect(g, PerkEquipLayout.MEDALLION_X, PerkEquipLayout.MEDALLION_Y, PerkEquipLayout.MEDALLION_W, PerkEquipLayout.MEDALLION_H, 0xFF8A6D3B, 0xFF3A2E1C);
			textC(g, tt("gui.witchercraft.shell.skills.medallion", "MED"), PerkEquipLayout.MEDALLION_X + PerkEquipLayout.MEDALLION_W / 2 - 8, PerkEquipLayout.MEDALLION_Y + PerkEquipLayout.MEDALLION_H / 2 - 4, 0xFFEEDDBB);
		}
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			int matches = groupMatchCount(gi);
			MutagenEffects.Type type = mutagenType(gi);
			int col = type != null ? type.tint() : TEXT_DIM;
			text(g, "g" + (gi + 1) + ":" + matches, PerkEquipLayout.SOCKET_X[gi] + 2, PerkEquipLayout.SOCKET_Y[gi] + PerkEquipLayout.SOCKET_SIZE + 1, col);
		}

		g.pose().popMatrix();

		// tooltips - stored, not drawn here (the shell renders them in screen space).
		// Hit-test in perk-local coords (map the mouse back through the fit scale).
		int lx = (int) ((mouseX - px) / scale), ly = (int) ((mouseY - py) / scale);
		int np = hitNode(lx, ly);
		int hoveredTab = hitTab(lx, ly);
		if (hoveredTab > 0) {
			pendingTooltip = List.of(tt(TAB_KEYS[hoveredTab - 1], TAB_FALLBACKS[hoveredTab - 1]));
		} else if (np > 0) {
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

	// Each perk has three flat MCreator-visible textures/screens/<slug>_<state>
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
		String key = slug + "_" + ICON_STATE[state];
		return ICON_CACHE.computeIfAbsent(key, k -> Identifier.parse("witchercraft:textures/screens/" + k + ".png"));
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
		for (int i = 0; i < TAB_ICONS.length; i++) {
			boolean active = activeBranch == i + 1;
			drawTexture(g, TAB_ICONS[i], TAB_X[i], TAB_Y, TAB_ICON);
			if (active)
				fill(g, TAB_X[i] - 1, TAB_Y + TAB_ICON + 1, TAB_X[i] + TAB_ICON + 1, TAB_Y + TAB_ICON + 2, tabAccent(i + 1));
			else
				fill(g, TAB_X[i], TAB_Y, TAB_X[i] + TAB_ICON, TAB_Y + TAB_ICON, TAB_DIM);
		}
	}

	private static int tabAccent(int tab) {
		return tab == TAB_MUTAGENS ? MUTAGENS_TAB_ACCENT : PerkRegistry.tint(tab);
	}

	private int hitTab(int lx, int ly) {
		for (int i = 0; i < TAB_ICONS.length; i++) {
			if (lx >= TAB_X[i] - 2 && lx < TAB_X[i] + TAB_ICON + 2 && ly >= TAB_Y - 2 && ly < TAB_Y + TAB_ICON + 3)
				return i + 1;
		}
		return -1;
	}

	/** A whole square texture (any source size, 16 for item textures) scaled into a size x size box. */
	private void drawTexture(GuiGraphicsExtractor g, String texture, int x, int y, int size) {
		float s = size / 16f;
		g.pose().pushMatrix();
		g.pose().translate((float) (ox + x), (float) (oy + y));
		g.pose().scale(s, s);
		g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(texture), 0, 0, 0, 0, 16, 16, 16, 16);
		g.pose().popMatrix();
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
			MutagenEffects.Type type = mutagenType(gi);
			if (type == null)
				continue;
			int m = type.branch;
			int col = withAlpha(type.tint(), 0xCC);
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
		float[] fit = layout(x, y, w, h);
		int lx = (int) ((mouseX - fit[0]) / fit[2]);
		int ly = (int) ((mouseY - fit[1]) / fit[2]);
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
			// mutagen sockets are real slots: SkillsScreen hands those clicks to the container
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

	// ---- helpers -------------------------------------------------------------

	/** Uniform scale to fit the 360x230 perk layout into a w x h content region. */
	private static float fitScale(int w, int h) {
		return Math.min((float) w / PerkEquipLayout.PANEL_W, (float) h / PerkEquipLayout.PANEL_H);
	}

	private int groupMatchCount(int gi) {
		Player entity = player();
		MutagenEffects.Type type = mutagenType(gi);
		if (entity == null || type == null)
			return 0;
		return MutagenEffects.synergy(entity, gi, type);
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

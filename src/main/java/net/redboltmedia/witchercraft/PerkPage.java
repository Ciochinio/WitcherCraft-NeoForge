package net.redboltmedia.witchercraft;

import java.util.List;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;

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
 * Drawn at normal GUI scale, 1:1 in screen pixels: the 360x200 panel is
 * centred in the region it is given ({@link #origin}) and every cell (tab, tree
 * node, equip slot, socket) is a vanilla-sized 18x18 frame holding a 16x16 icon,
 * so perk glyphs, mutagens and inventory items all line up. Draw helpers offset
 * by the panel origin (ox, oy). The perk recompute runs server-side after each
 * equip change (see PerkEquipGuiButtonMessage).
 */
public class PerkPage implements GuiPage {

	// Every colour comes from PerkEquipLayout (edited in equip-grid-placer's Colours panel).
	private static final int TEXT_HELD = 0xFFFFFFFF;

	// Sub-tabs (index i -> activeBranch i+1): the four perk branches
	// (Combat/Alchemy/Signs/General = branch colours 1-4), then Mutagens, which
	// shows the player inventory instead of a tree. Icons are 16x16 placeholder
	// textures (copies of vanilla item art) meant to be painted over in place.
	// Positions come from PerkTree (TAB_X / TAB_Y, placed in tree-node-placer).
	public static final int TAB_MUTAGENS = 5;
	private static final String[] TAB_KEYS = {
			"gui.witchercraft.shell.skills.tab_combat", "gui.witchercraft.shell.skills.tab_alchemy",
			"gui.witchercraft.shell.skills.tab_signs", "gui.witchercraft.shell.skills.tab_general",
			"gui.witchercraft.shell.skills.tab_mutagens"};
	private static final String[] TAB_FALLBACKS = {"Combat", "Alchemy", "Signs", "General", "Mutagens"};
	private static final String[] TAB_ICONS = {
			"witchercraft:textures/screens/skills_tab_combat.png", "witchercraft:textures/screens/skills_tab_alchemy.png",
			"witchercraft:textures/screens/skills_tab_signs.png", "witchercraft:textures/screens/skills_tab_general.png",
			"witchercraft:textures/screens/skills_tab_mutagens.png"};

	/** Every cell is a vanilla slot: an 18x18 frame around a 16x16 icon. */
	public static final int CELL = 18, ICON = 16;

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
	 * Top-left of the 360x200 panel centred in a w x h region (screen pixels, no
	 * scaling). Never above the region's top; it overflows the bottom only when
	 * the region is too short.
	 */
	public static int[] origin(int x, int y, int w, int h) {
		return new int[]{x + (w - PerkEquipLayout.PANEL_W) / 2, y + Math.max(0, (h - PerkEquipLayout.PANEL_H) / 2)};
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
		// which perks exist and which are learned: one AllPerks scan per frame (PerkCatalog)
		PerkCatalog.refreshClient(entity);

		// the panel at 1:1 GUI scale, centred in the region; helpers offset by (ox, oy)
		int[] o = origin(x, y, w, h);
		this.ox = o[0];
		this.oy = o[1];

		drawTabs(g);
		// status texts: the skill points readout and the instructions (Mutagens tab
		// has its own). Wording is in the lang file; position and scale are
		// PerkTree data (tree-node-placer), colours PerkEquipLayout.
		WitchercraftModVariables.PlayerVariables vars = entity.getData(WitchercraftModVariables.PLAYER_VARIABLES);
		// level 1 starts with 0 points; clamped so a level reset never shows negatives
		String points = new java.text.DecimalFormat("##.##").format(Math.max(0, vars.witchercraftPlayerLevel - 1 - vars.witchercraftPerksLearned));
		drawScaledText(g, Component.translatableWithFallback("gui.witchercraft.shell.skills.points", "SP available: %s", points),
				PerkTree.POINTS_X, PerkTree.POINTS_Y, PerkTree.POINTS_SCALE, PerkEquipLayout.STATUS_POINTS);
		Component hint = mutagensShown()
				? tt("gui.witchercraft.shell.skills.mutagen_instructions", "Drag a mutagen into a socket")
				: tt("gui.witchercraft.shell.skills.instructions", "R-click=learn  L-click learned=hold");
		drawScaledText(g, hint, PerkTree.HINT_X, PerkTree.HINT_Y, PerkTree.HINT_SCALE, PerkEquipLayout.STATUS_TEXT);

		// the Mutagens tab leaves the tree area to the inventory slots SkillsScreen draws
		if (!mutagensShown())
			drawTree(g, entity);
		drawConnectors(g, entity);

		// equip grid slots
		for (int i = 0; i < PerkEquipVars.PERK_SLOTS; i++) {
			int sx = PerkEquipLayout.SLOT_X[i];
			int sy = PerkEquipLayout.SLOT_Y[i];
			int cur = PerkEquipVars.getPerkSocket(entity, i);
			// a slot below its witcher level shows the lock glyph and the level it
			// opens at (a perk already in it after a level reset still shows)
			if (cur <= 0 && !PerkEquipVars.isSlotUnlocked(entity, i)) {
				drawLockedSlot(g, sx, sy, PerkEquipVars.slotUnlockLevel(i));
				continue;
			}
			// while holding, EVERY open slot is a legal target (empty = place,
			// occupied = swap, evicting the occupant back to the pool).
			boolean validTarget = heldPerk != 0 && PerkEquipVars.isSlotUnlocked(entity, i);
			if (cur > 0) {
				int c = PerkRegistry.tint(PerkRegistry.color(cur));
				drawCell(g, sx, sy, PerkEquipLayout.SLOT_SIZE, validTarget ? PerkEquipLayout.TARGET_BORDER : c, withAlpha(c, 0x55));
				drawPerkIcon(g, cur, sx, sy, ICON_EQUIPPED); // socketed = equipped
			} else {
				drawCell(g, sx, sy, PerkEquipLayout.SLOT_SIZE, validTarget ? PerkEquipLayout.TARGET_BORDER : PerkEquipLayout.CELL_BORDER, PerkEquipLayout.CELL_INNER);
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
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, c, PerkEquipLayout.CELL_INNER);
			} else {
				drawCell(g, sx, sy, PerkEquipLayout.SOCKET_SIZE, PerkEquipLayout.CELL_BORDER, PerkEquipLayout.CELL_INNER);
			}
		}

		// medallion + what each socketed mutagen gives
		if (PerkEquipLayout.MEDALLION_ENABLED) {
			drawRect(g, PerkEquipLayout.MEDALLION_X, PerkEquipLayout.MEDALLION_Y, PerkEquipLayout.MEDALLION_W, PerkEquipLayout.MEDALLION_H, PerkEquipLayout.MEDALLION_BORDER, PerkEquipLayout.MEDALLION_INNER);
			textC(g, tt("gui.witchercraft.shell.skills.medallion", "MED"), PerkEquipLayout.MEDALLION_X + PerkEquipLayout.MEDALLION_W / 2 - 8, PerkEquipLayout.MEDALLION_Y + PerkEquipLayout.MEDALLION_H / 2 - 4, PerkEquipLayout.MEDALLION_TEXT);
		}
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++)
			drawBonus(g, entity, gi);

		// tooltips - stored, not drawn here (the screen renders them last).
		// Hit-test in perk-local coords.
		int lx = mouseX - ox, ly = mouseY - oy;
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
				else if (!PerkEquipVars.isSlotUnlocked(entity, si))
					pendingTooltip = List.of(Component.translatableWithFallback("gui.witchercraft.shell.skills.slot_locked", "Unlocks at level %s", PerkEquipVars.slotUnlockLevel(si)));
			} else {
				Component stat = hitBonus(entity, lx, ly);
				if (stat != null)
					pendingTooltip = List.of(stat);
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
		if (PerkRegistry.name(perkId).isEmpty()) // a tree slot no perk procedure claims yet
			return List.of(Component.literal("Slot " + perkId + ": no perk"));
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

	/** One line of text at panel-local (x, y), its top-left, scaled about that corner. */
	private void drawScaledText(GuiGraphicsExtractor g, Component c, float x, float y, float scale, int col) {
		g.pose().pushMatrix();
		g.pose().translate(ox + x, oy + y);
		g.pose().scale(scale, scale);
		g.text(font, c, 0, 0, col, false);
		g.pose().popMatrix();
	}

	// shorthand for a translatable with a readable fallback (see PerkRegistry.fallbackName).
	private static Component tt(String key, String fallback) {
		return Component.translatableWithFallback(key, fallback);
	}

	// ---- perk icons (32x32 source, drawn 16x16 inside an 18x18 cell) --------

	// Each perk has three flat MCreator-visible textures/screens/<slug>_<state>
	// glyphs. The coloured frame / selection highlight is drawn by the GUI, not
	// baked into the glyph, so these are bare icons.
	private static final int ICON_NOTLEARNED = 0, ICON_NOTEQUIPPED = 1, ICON_EQUIPPED = 2;
	private static final String[] ICON_STATE = {"notlearned", "notequipped", "equipped"};

	// Source glyphs are 32x32; they are squeezed into the 16x16 icon area.
	private static final int ICON_SRC = 32;
	private static final Map<String, Identifier> ICON_CACHE = new HashMap<>();

	private static Identifier perkIcon(int perkId, int state) {
		String slug = PerkRegistry.slug(perkId);
		if (slug.isEmpty())
			return null;
		String key = slug + "_" + ICON_STATE[state];
		return ICON_CACHE.computeIfAbsent(key, k -> Identifier.parse("witchercraft:textures/screens/" + k + ".png"));
	}

	/** Draw a perk's glyph in the 16x16 icon area of the cell at (x, y). A slot no
	 *  perk procedure claims yet shows its id instead. */
	private void drawPerkIcon(GuiGraphicsExtractor g, int perkId, int x, int y, int state) {
		Identifier id = perkIcon(perkId, state);
		if (id == null) {
			text(g, String.valueOf(perkId), x, y + 5, state == ICON_NOTLEARNED ? PerkEquipLayout.STATUS_TEXT : TEXT_HELD);
			return;
		}
		g.blit(RenderPipelines.GUI_TEXTURED, id, ox + x + 1, oy + y + 1, 0, 0, ICON, ICON, ICON_SRC, ICON_SRC, ICON_SRC, ICON_SRC);
	}

	// Placeholder art for a perk slot below its witcher level (16x16, bare like
	// the perk glyphs); the level it opens at is drawn over it as text.
	private static final Identifier SLOT_LOCKED_ICON = Identifier.parse("witchercraft:textures/screens/perk_slot_locked.png");

	private void drawLockedSlot(GuiGraphicsExtractor g, int x, int y, int level) {
		drawCell(g, x, y, PerkEquipLayout.SLOT_SIZE, PerkEquipLayout.NODE_LOCKED_BORDER, PerkEquipLayout.NODE_LOCKED_INNER);
		g.blit(RenderPipelines.GUI_TEXTURED, SLOT_LOCKED_ICON, ox + x + 1, oy + y + 1, 0, 0, ICON, ICON, ICON, ICON);
		Component label = Component.translatableWithFallback("gui.witchercraft.shell.skills.slot_level", "%s", level);
		// centre the visible glyphs: font.width counts a trailing 1px gap and digits
		// are 7px tall, so either can land on a half pixel (the pose takes floats)
		float tx = x + (PerkEquipLayout.SLOT_SIZE - (font.width(label) - 1)) / 2f;
		float ty = y + (PerkEquipLayout.SLOT_SIZE - 7) / 2f;
		g.pose().pushMatrix();
		g.pose().translate(ox + tx, oy + ty);
		g.text(font, label, 0, 0, PerkEquipLayout.STATUS_TEXT, true);
		g.pose().popMatrix();
	}

	// ---- left panel: perk tree ----------------------------------------------

	private void drawTabs(GuiGraphicsExtractor g) {
		for (int i = 0; i < TAB_ICONS.length; i++) {
			boolean active = activeBranch == i + 1;
			int tx = PerkTree.TAB_X[i], ty = PerkTree.TAB_Y;
			// the active tab is marked by its frame colour alone
			drawCell(g, tx, ty, CELL, active ? PerkEquipLayout.TAB_ACTIVE_BORDER[i] : PerkEquipLayout.TAB_BORDER, PerkEquipLayout.TAB_INNER);
			g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(TAB_ICONS[i]), ox + tx + 1, oy + ty + 1, 0, 0, ICON, ICON, ICON, ICON, active ? -1 : PerkEquipLayout.TAB_FADE);
		}
	}

	private int hitTab(int lx, int ly) {
		for (int i = 0; i < TAB_ICONS.length; i++) {
			int tx = PerkTree.TAB_X[i], ty = PerkTree.TAB_Y;
			if (lx >= tx && lx < tx + CELL && ly >= ty && ly < ty + CELL)
				return i + 1;
		}
		return -1;
	}

	private void drawTree(GuiGraphicsExtractor g, Player entity) {
		List<PerkTree.Node> nodes = PerkTree.forColor(activeBranch);
		int tint = PerkRegistry.tint(activeBranch);
		for (PerkTree.Node n : nodes) {
			boolean childLearned = PerkCatalog.isLearnedClient(n.perkId);
			boolean childMet = prereqsMet(n);
			int col = childLearned ? withAlpha(tint, 0xCC) : (childMet ? withAlpha(tint, 0x77) : PerkEquipLayout.LINK_LOCKED);
			for (int pre : n.prereqs) {
				PerkTree.Node p = PerkTree.byId(pre);
				if (p == null)
					continue;
				hLine(g, p.cx(), n.cx(), p.cy(), col);
				vLine(g, n.cx(), p.cy(), n.cy(), col);
			}
		}
		for (PerkTree.Node n : nodes) {
			boolean learned = PerkCatalog.isLearnedClient(n.perkId);
			boolean equipped = learned && PerkEquipVars.isPerkSocketed(entity, n.perkId);
			boolean met = prereqsMet(n);
			int t = PerkRegistry.tint(PerkRegistry.color(n.perkId));
			if (heldPerk == n.perkId) // 1px selection ring (matches the slot frames)
				drawRect(g, n.x - 1, n.y - 1, PerkTree.NODE_SIZE + 2, PerkTree.NODE_SIZE + 2, PerkEquipLayout.TARGET_BORDER, PerkEquipLayout.TARGET_BORDER);
			int border, inner;
			if (equipped) {
				border = t;
				inner = withAlpha(t, 0x99);
			} else if (learned) {
				border = t;
				inner = PerkEquipLayout.CELL_INNER;
			} else if (met) {
				border = dim(t);
				inner = PerkEquipLayout.NODE_AVAILABLE_INNER;
			} else {
				border = PerkEquipLayout.NODE_LOCKED_BORDER;
				inner = PerkEquipLayout.NODE_LOCKED_INNER;
			}
			drawCell(g, n.x, n.y, PerkTree.NODE_SIZE, border, inner);
			// three-state glyph: locked/available -> notlearned, learned but not
			// slotted -> notequipped, slotted -> equipped.
			int state = equipped ? ICON_EQUIPPED : (learned ? ICON_NOTEQUIPPED : ICON_NOTLEARNED);
			drawPerkIcon(g, n.perkId, n.x, n.y, state);
		}
	}

	// A node's prereqs are an OR group (PerkTree.prereqsMet), the same check the
	// server runs in PerkEquipGuiButtonMessage - see TDD 3.10.
	private boolean prereqsMet(PerkTree.Node n) {
		return PerkTree.prereqsMet(n, PerkCatalog::isLearnedClient);
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
		// 1px border as four edges, so a translucent inner never shows the border colour through
		fill(g, x, y, x + w, y + 1, border);
		fill(g, x, y + h - 1, x + w, y + h, border);
		fill(g, x, y + 1, x + 1, y + h - 1, border);
		fill(g, x + w - 1, y + 1, x + w, y + h - 1, border);
		fill(g, x + 1, y + 1, x + w - 1, y + h - 1, inner);
	}

	// ---- input ---------------------------------------------------------------

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		Player entity = player();
		if (entity == null)
			return false;
		int[] o = origin(x, y, w, h);
		int lx = (int) mouseX - o[0];
		int ly = (int) mouseY - o[1];
		int nodePerk = hitNode(lx, ly);
		if (button == 1) { // right-click a node = learn (server enforces prereqs + points)
			if (nodePerk > 0) {
				if (!PerkCatalog.isLearnedClient(nodePerk))
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
				if (PerkCatalog.isLearnedClient(nodePerk))
					heldPerk = nodePerk;
				return true;
			}
			int si = hitSlot(lx, ly);
			if (si >= 0) {
				int cur = PerkEquipVars.getPerkSocket(entity, si);
				if (heldPerk != 0 && !PerkEquipVars.isSlotUnlocked(entity, si)) {
					return true; // locked slot: keep holding, nothing to send
				} else if (heldPerk != 0) {
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

	/**
	 * Socket gi's bonus card: one BONUS_ROW_H row per modifier, stacked down from
	 * (BONUS_X, BONUS_Y), each the nine-slice frame tinted for the mutagen type,
	 * the stat's icon at the BONUS_ICON_RIGHT side and the amount centred in the
	 * rest. The stat name is the row's tooltip. Nothing for an empty socket.
	 */
	private void drawBonus(GuiGraphicsExtractor g, Player entity, int gi) {
		MutagenEffects.Type type = mutagenType(gi);
		if (type == null)
			return;
		int t = type.ordinal();
		List<MutagenEffects.Bonus> rows = MutagenEffects.bonusLines(entity, gi, type);
		int bx = PerkEquipLayout.BONUS_X[gi], bw = PerkEquipLayout.BONUS_W[gi], rh = PerkEquipLayout.BONUS_ROW_H;
		boolean iconRight = PerkEquipLayout.BONUS_ICON_RIGHT[gi];
		float s = PerkEquipLayout.BONUS_TEXT_SCALE;
		int iconX = iconRight ? bx + bw - BONUS_PAD - ICON : bx + BONUS_PAD;
		// the value's area: the card minus the padding and the icon with its gap
		int areaX = iconRight ? bx + BONUS_PAD : iconX + ICON + BONUS_ICON_GAP;
		int areaW = bw - 2 * BONUS_PAD - ICON - BONUS_ICON_GAP;
		for (int i = 0; i < rows.size(); i++) {
			MutagenEffects.Bonus row = rows.get(i);
			int ry = bonusRowY(gi, i);
			g.blitSprite(RenderPipelines.GUI_TEXTURED, BONUS_FRAME, ox + bx, oy + ry, bw, rh, PerkEquipLayout.BONUS_FRAME[t]);
			g.blit(RenderPipelines.GUI_TEXTURED, type.bonusIcon(), ox + iconX, oy + ry + (rh - ICON) / 2, 0, 0, ICON, ICON, ICON, ICON);
			// value centred on its visible glyphs (7px tall, width minus the trailing gap)
			float vx = areaX + (areaW - (font.width(row.value()) - 1) * s) / 2f;
			float vy = ry + (rh - 7 * s) / 2f;
			drawScaledText(g, Component.literal(row.value()), vx, vy, s, PerkEquipLayout.BONUS_VALUE[t]);
		}
	}

	private static int bonusRowY(int gi, int row) {
		return PerkEquipLayout.BONUS_Y[gi] + row * (PerkEquipLayout.BONUS_ROW_H + BONUS_ROW_GAP);
	}

	/** The stat name of the bonus row under (lx, ly), or null. */
	private Component hitBonus(Player entity, int lx, int ly) {
		for (int gi = 0; gi < PerkEquipVars.MUTAGEN_GROUPS; gi++) {
			MutagenEffects.Type type = mutagenType(gi);
			if (type == null)
				continue;
			int bx = PerkEquipLayout.BONUS_X[gi];
			if (lx < bx || lx >= bx + PerkEquipLayout.BONUS_W[gi])
				continue;
			List<MutagenEffects.Bonus> rows = MutagenEffects.bonusLines(entity, gi, type);
			for (int i = 0; i < rows.size(); i++) {
				int ry = bonusRowY(gi, i);
				if (ly >= ry && ly < ry + PerkEquipLayout.BONUS_ROW_H)
					return rows.get(i).label();
			}
		}
		return null;
	}

	private static final Identifier BONUS_FRAME = Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "mutagen_bonus_frame");
	private static final int BONUS_PAD = 4, BONUS_ICON_GAP = 4, BONUS_ROW_GAP = 2;

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

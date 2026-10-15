package net.redboltmedia.witchercraft;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.redboltmedia.witchercraft.mixin.LivingEntityRendererAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * The Glossary tab: two sub-tabs laid out in the same three columns, so nothing
 * ever scrolls - lists and text that outgrow their panel are split into pages.
 *
 * - Bestiary: a rail of monster categories ({@link GlossaryBestiary}, one per
 *   category tag), the creatures in the selected one, a 3D doll of the selected
 *   creature (fitted to the doll area, drag to rotate) with the oils it is weak
 *   to, and an info panel with its stats and one description.
 * - Tutorials: topics grouped by system ({@link GlossaryTutorials}), a large
 *   illustration, and the topic's text in the same info panel.
 *
 * Geometry and colours are {@link GlossaryLayout} (tools/glossary-layout-creator.html).
 * Styling follows the rest of the shell: sub-tabs like the navbar, panels like the
 * map, cells like the skills tree. Pure client state; the selection is remembered
 * for the session. The mouse wheel turns pages wherever there are pages.
 */
public class GlossaryPage implements GuiPage {

	private enum SubTab {
		BESTIARY("bestiary"), TUTORIALS("tutorials");

		private final String key;

		SubTab(String key) {
			this.key = key;
		}

		Component title() {
			return Component.translatable("glossary.witchercraft.tab." + key);
		}
	}

	/** A page footer: prev button, "page / pages", next button. */
	private record Pager(int prevX, int nextX, int y, int labelX, Component label, int page, int pages) {
	}

	/**
	 * A posed model's extents in blocks, feet at y 0: its lowest and highest point, and the
	 * largest distance of any point from the vertical axis the doll turns around (so the
	 * fitted doll stays inside its area at every angle).
	 */
	private record DollFit(float minY, float maxY, float radius) {
	}

	private static final DecimalFormat NUMBER = new DecimalFormat("0.##");
	private static final int LOCK_FADE = 0xAA101015;
	private static final int DOLL_DRAG_DEGREES_PER_PX = 2;

	// --- session state (static: survives closing and reopening the menu) -----
	private static SubTab subTab = SubTab.BESTIARY;
	private static GlossaryBestiary.Category category;
	private static EntityType<?> selectedType;
	private static int listPage;
	private static int textPage;
	private static String topicId;
	private static float dollYaw = GlossaryLayout.DOLL_YAW;

	private final String pageId;
	private boolean draggingDoll;
	// page count of the text drawn last frame, so clicks and the wheel know the bounds
	private int lastTextPages = 1;
	private List<Component> pendingTooltip;
	private final Map<Identifier, Boolean> imageExists = new HashMap<>();
	private final Set<EntityType<?>> brokenDolls = new HashSet<>();
	private final Map<EntityType<?>, DollFit> dollFits = new HashMap<>();

	public GlossaryPage(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public String id() {
		return pageId;
	}

	@Override
	public Component navLabel() {
		return Component.translatable("gui.witchercraft.shell.nav.glossary");
	}

	@Override
	public void onShown() {
		GlossaryBestiary.refresh();
		imageExists.clear();
		dollFits.clear();
		draggingDoll = false;
	}

	@Override
	public void onHidden() {
		draggingDoll = false;
	}

	@Override
	public List<Component> pollTooltip() {
		List<Component> tip = pendingTooltip;
		pendingTooltip = null;
		return tip;
	}

	// ==========================================================================
	// rendering
	// ==========================================================================

	@Override
	public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		Font font = Minecraft.getInstance().font;
		pendingTooltip = null;
		drawSubTabs(g, font, x, y, w, mouseX, mouseY);
		if (subTab == SubTab.BESTIARY)
			renderBestiary(g, font, x, y, mouseX, mouseY);
		else
			renderTutorials(g, font, x, y, mouseX, mouseY);
	}

	// ---- sub-tabs (navbar style: translucent tab, top and bottom rule, gold underline)

	private static int subTabX(int x, int w, int i) {
		int n = SubTab.values().length;
		int total = n * GlossaryLayout.SUBTAB_W + (n - 1) * GlossaryLayout.SUBTAB_GAP;
		return x + (w - total) / 2 + i * (GlossaryLayout.SUBTAB_W + GlossaryLayout.SUBTAB_GAP);
	}

	private void drawSubTabs(GuiGraphicsExtractor g, Font font, int x, int y, int w, int mouseX, int mouseY) {
		int ty = y + GlossaryLayout.SUBTAB_Y, tw = GlossaryLayout.SUBTAB_W, th = GlossaryLayout.SUBTAB_H;
		for (SubTab tab : SubTab.values()) {
			int tx = subTabX(x, w, tab.ordinal());
			boolean active = tab == subTab;
			boolean hover = MapPage.inside(mouseX, mouseY, tx, ty, tw, th);
			g.fill(tx, ty, tx + tw, ty + th, active ? GlossaryLayout.SUBTAB_BG_ACTIVE : GlossaryLayout.SUBTAB_BG);
			g.fill(tx, ty, tx + tw, ty + 1, GlossaryLayout.SUBTAB_BORDER);
			g.fill(tx, ty + th - 1, tx + tw, ty + th, GlossaryLayout.SUBTAB_BORDER);
			Component label = tab.title();
			g.text(font, label, tx + (tw - font.width(label)) / 2, ty + (th - 8) / 2, active || hover ? GlossaryLayout.SUBTAB_TEXT_ACTIVE : GlossaryLayout.SUBTAB_TEXT, false);
			if (active)
				g.fill(tx + 3, ty + th, tx + tw - 3, ty + th + 1, GlossaryLayout.ACCENT);
		}
	}

	// ---- Bestiary --------------------------------------------------------------

	private void renderBestiary(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY) {
		GlossaryBestiary.Category cat = currentCategory();
		List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(cat);
		GlossaryBestiary.Entry entry = selectedEntry(entries);

		drawRail(g, x, y, cat, mouseX, mouseY);
		drawEntryList(g, font, x, y, cat, entries, entry, mouseX, mouseY);
		drawDollColumn(g, font, x, y, entry, mouseX, mouseY);
		drawEntryInfo(g, font, x, y, entry, mouseX, mouseY);
	}

	private static int railCellX(int x) {
		return x + GlossaryLayout.RAIL_X + (GlossaryLayout.RAIL_W - GlossaryLayout.RAIL_CELL) / 2;
	}

	private static int railCellY(int y, int i) {
		return y + GlossaryLayout.RAIL_Y + GlossaryLayout.RAIL_PAD_Y + i * GlossaryLayout.RAIL_STEP;
	}

	private void drawRail(GuiGraphicsExtractor g, int x, int y, GlossaryBestiary.Category current, int mouseX, int mouseY) {
		panel(g, x + GlossaryLayout.RAIL_X, y + GlossaryLayout.RAIL_Y, GlossaryLayout.RAIL_W, GlossaryLayout.RAIL_H);
		int size = GlossaryLayout.RAIL_CELL;
		for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values()) {
			int cx = railCellX(x), cy = railCellY(y, c.ordinal());
			boolean active = c == current;
			boolean hover = MapPage.inside(mouseX, mouseY, cx, cy, size, size);
			cell(g, cx, cy, size, active ? GlossaryLayout.ACCENT : hover ? GlossaryLayout.TEXT_DIM : GlossaryLayout.CELL_BORDER);
			int ix = cx + (size - 16) / 2, iy = cy + (size - 16) / 2;
			g.fakeItem(c.oil(), ix, iy);
			if (GlossaryBestiary.entries(c).isEmpty() && !active)
				g.fill(ix, iy, ix + 16, iy + 16, LOCK_FADE);
			if (hover)
				pendingTooltip = List.of(c.title());
		}
	}

	private static int listRowY(int y, int row) {
		return y + GlossaryLayout.LIST_Y + GlossaryLayout.LIST_HEADER_H + 2 + row * GlossaryLayout.LIST_ROW_H;
	}

	private static int listPages(int count) {
		return Math.max(1, (count + GlossaryLayout.LIST_ROWS - 1) / GlossaryLayout.LIST_ROWS);
	}

	private void drawEntryList(GuiGraphicsExtractor g, Font font, int x, int y, GlossaryBestiary.Category cat, List<GlossaryBestiary.Entry> entries,
			GlossaryBestiary.Entry selected, int mouseX, int mouseY) {
		int lx = x + GlossaryLayout.LIST_X, ly = y + GlossaryLayout.LIST_Y, lw = GlossaryLayout.LIST_W, lh = GlossaryLayout.LIST_H;
		panel(g, lx, ly, lw, lh);
		g.text(font, cat.title(), lx + GlossaryLayout.INFO_PAD, ly + (GlossaryLayout.LIST_HEADER_H - 8) / 2, GlossaryLayout.TITLE, false);
		divider(g, lx, ly + GlossaryLayout.LIST_HEADER_H - 1, lw);

		int pages = listPages(entries.size());
		listPage = clamp(listPage, pages);
		int first = listPage * GlossaryLayout.LIST_ROWS;
		int rowW = lw - 6, rowH = GlossaryLayout.LIST_ROW_H - 2;
		for (int i = first; i < entries.size() && i < first + GlossaryLayout.LIST_ROWS; i++) {
			GlossaryBestiary.Entry entry = entries.get(i);
			int rx = lx + 3, ry = listRowY(y, i - first);
			boolean isSelected = entry.equals(selected);
			boolean hover = MapPage.inside(mouseX, mouseY, rx, ry, rowW, rowH);
			if (isSelected) {
				g.fill(rx, ry, rx + rowW, ry + rowH, GlossaryLayout.ROW_SELECTED);
				g.fill(rx, ry, rx + 2, ry + rowH, GlossaryLayout.ACCENT);
			} else if (hover) {
				g.fill(rx, ry, rx + rowW, ry + rowH, GlossaryLayout.ROW_HOVER);
			}
			ItemStack icon = entry.icon();
			if (!icon.isEmpty())
				g.fakeItem(icon, rx + 5, ry + (rowH - 16) / 2);
			String name = entry.name().getString();
			String shown = fit(font, name, rowW - 30);
			g.text(font, Component.literal(shown), rx + 26, ry + (rowH - 8) / 2, isSelected ? GlossaryLayout.SUBTAB_TEXT_ACTIVE : GlossaryLayout.TEXT, false);
			if (hover && !shown.equals(name))
				pendingTooltip = List.of(entry.name());
		}
		if (entries.isEmpty())
			g.text(font, Component.translatable("glossary.witchercraft.empty_category"), lx + GlossaryLayout.INFO_PAD, listRowY(y, 0) + 6, GlossaryLayout.TEXT_DIM, false);

		if (pages > 1)
			drawPager(g, font, listPager(font, x, y, pages), mouseX, mouseY);
	}

	private Pager listPager(Font font, int x, int y, int pages) {
		int right = x + GlossaryLayout.LIST_X + GlossaryLayout.LIST_W - 4;
		int fy = y + GlossaryLayout.LIST_Y + GlossaryLayout.LIST_H - GlossaryLayout.FOOTER_H;
		return pager(font, right - pagerWidth(font, pages), fy, listPage, pages);
	}

	private void drawDollColumn(GuiGraphicsExtractor g, Font font, int x, int y, GlossaryBestiary.Entry entry, int mouseX, int mouseY) {
		if (entry == null)
			return;
		int dx = x + GlossaryLayout.DOLL_X, dy = y + GlossaryLayout.DOLL_Y, dw = GlossaryLayout.DOLL_W, dh = GlossaryLayout.DOLL_H;
		LivingEntity doll = brokenDolls.contains(entry.type()) ? null : GlossaryBestiary.doll(entry.type());
		if (doll != null) {
			drawDoll(g, doll, entry.type(), dx, dy, dw, dh);
		} else {
			// not a living creature (or its renderer failed): its spawn egg, large
			ItemStack icon = entry.icon();
			if (!icon.isEmpty())
				drawScaledItem(g, icon, dx + dw / 2, dy + dh / 2, GlossaryLayout.IMAGE_ICON_SCALE);
		}
		if (MapPage.inside(mouseX, mouseY, dx, dy, dw, dh) || draggingDoll) {
			Component hint = Component.translatable("glossary.witchercraft.drag_hint");
			g.text(font, hint, dx + (dw - font.width(hint)) / 2, dy + dh - 10, GlossaryLayout.TEXT_DIM, false);
		}

		// "Weak to" and one cell per oil, centred under the doll
		List<ItemStack> oils = weaknesses(entry);
		if (oils.isEmpty())
			return;
		Component label = Component.translatable("glossary.witchercraft.weak_to");
		int cx = dx + dw / 2, wy = y + GlossaryLayout.WEAK_Y;
		g.text(font, label, cx - font.width(label) / 2, wy, GlossaryLayout.TEXT, false);
		int size = GlossaryLayout.WEAK_CELL, gap = GlossaryLayout.WEAK_GAP;
		int total = oils.size() * size + (oils.size() - 1) * gap;
		for (int i = 0; i < oils.size(); i++) {
			int ox = cx - total / 2 + i * (size + gap), oy = wy + 12;
			boolean hover = MapPage.inside(mouseX, mouseY, ox, oy, size, size);
			cell(g, ox, oy, size, hover ? GlossaryLayout.ACCENT : GlossaryLayout.CELL_BORDER);
			g.fakeItem(oils.get(i), ox + (size - 16) / 2, oy + (size - 16) / 2);
			if (hover)
				pendingTooltip = List.of(oils.get(i).getHoverName());
		}
	}

	/**
	 * The creature's 3D model, scaled so its whole posed model (not just its hitbox, which a
	 * horse's head or a spider's legs reach well past) fits DOLL_FILL_PERCENT of the box at
	 * any angle, and centred in it. Entities are drawn as a picture-in-picture in SCREEN
	 * pixels, so the box is mapped through the shell's design->screen transform first.
	 */
	private void drawDoll(GuiGraphicsExtractor g, LivingEntity doll, EntityType<?> type, int bx, int by, int bw, int bh) {
		Vector2f a = g.pose().transformPosition(bx, by, new Vector2f());
		Vector2f b = g.pose().transformPosition(bx + bw, by + bh, new Vector2f());
		int x0 = Math.round(a.x), y0 = Math.round(a.y), x1 = Math.round(b.x), y1 = Math.round(b.y);
		try {
			EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(doll);
			EntityRenderState state = renderer.createRenderState(doll, 1.0F);
			state.shadowPieces.clear();
			state.outlineColor = 0;
			if (state instanceof LivingEntityRenderState living) {
				living.bodyRot = 180.0F + dollYaw;
				living.yRot = 0.0F;
				living.xRot = 0.0F;
				living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
				living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
				living.scale = 1.0F;
			}
			DollFit fit = dollFits.computeIfAbsent(type, t -> measure(renderer, state, doll));
			float fill = GlossaryLayout.DOLL_FILL_PERCENT / 100f;
			float height = Math.max(fit.maxY() - fit.minY(), 0.1f), width = Math.max(2 * fit.radius(), 0.1f);
			float size = fill * Math.min((y1 - y0) / height, (x1 - x0) / width);
			Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
			Vector3f translation = new Vector3f(0.0F, (fit.minY() + fit.maxY()) / 2.0F, 0.0F);
			g.entity(state, size, translation, rotation, new Quaternionf(), x0, y0, x1, y1);
		} catch (RuntimeException e) {
			brokenDolls.add(type);
			WitchercraftMod.LOGGER.warn("Glossary could not draw the doll of {}", type, e);
		}
	}

	/**
	 * Measure the posed model the way {@code LivingEntityRenderer} draws it (flip, the renderer's
	 * own scale, the 1.501 lift), through vanilla's {@code getExtentsForGui}. Layers (saddles,
	 * armour, held items) are not counted. Falls back to the hitbox for non-model renderers.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static DollFit measure(EntityRenderer<?, ?> renderer, EntityRenderState state, LivingEntity doll) {
		if (renderer instanceof LivingEntityRenderer living && state instanceof LivingEntityRenderState livingState) {
			try {
				EntityModel model = living.getModel();
				model.setupAnim(livingState);
				PoseStack pose = new PoseStack();
				pose.scale(-1.0F, -1.0F, 1.0F);
				((LivingEntityRendererAccessor) living).witchercraft$scale(livingState, pose);
				pose.translate(0.0F, -1.501F, 0.0F);
				float[] e = {Float.MAX_VALUE, -Float.MAX_VALUE, 0.0F};
				model.root().getExtentsForGui(pose, p -> {
					e[0] = Math.min(e[0], p.y());
					e[1] = Math.max(e[1], p.y());
					e[2] = Math.max(e[2], (float) Math.sqrt(p.x() * p.x() + p.z() * p.z()));
				});
				if (e[1] > e[0])
					return new DollFit(e[0], e[1], e[2]);
			} catch (RuntimeException ex) {
				WitchercraftMod.LOGGER.warn("Glossary could not measure the model of {}", doll.getType(), ex);
			}
		}
		return new DollFit(0.0F, doll.getBbHeight(), doll.getBbWidth() * 0.71F);
	}

	private void drawEntryInfo(GuiGraphicsExtractor g, Font font, int x, int y, GlossaryBestiary.Entry entry, int mouseX, int mouseY) {
		int ix = x + GlossaryLayout.INFO_X, iy = y + GlossaryLayout.INFO_Y, iw = GlossaryLayout.INFO_W;
		panel(g, ix, iy, iw, GlossaryLayout.INFO_H);
		if (entry == null)
			return;
		drawTitle(g, font, entry.name(), ix, iy, iw);

		// stats: a fixed block on every page, ruled off from the description
		List<Component> stats = stats(entry);
		int sy = iy + GlossaryLayout.BODY_Y;
		for (int i = 0; i < stats.size(); i++)
			g.text(font, stats.get(i), ix + GlossaryLayout.INFO_PAD, sy + i * GlossaryLayout.LINE_H, GlossaryLayout.TEXT, false);
		int bodyY = GlossaryLayout.BODY_Y;
		if (!stats.isEmpty()) {
			bodyY += stats.size() * GlossaryLayout.LINE_H + GlossaryLayout.STATS_GAP;
			divider(g, ix, iy + bodyY - GlossaryLayout.STATS_GAP / 2 - 1, iw);
		}

		String key = entry.descriptionKey();
		Component description = I18n.exists(key) ? Component.translatable(key) : Component.translatable("glossary.witchercraft.no_notes").withColor(GlossaryLayout.TEXT_DIM);
		drawPagedText(g, font, x, y, font.split(description, iw - 2 * GlossaryLayout.INFO_PAD), bodyY, mouseX, mouseY);
	}

	/** Health, damage and armor, read live from the doll (none for a creature that is not living). */
	private static List<Component> stats(GlossaryBestiary.Entry entry) {
		List<Component> stats = new ArrayList<>();
		LivingEntity doll = GlossaryBestiary.doll(entry.type());
		if (doll != null) {
			addStat(stats, "health", doll, Attributes.MAX_HEALTH);
			addStat(stats, "damage", doll, Attributes.ATTACK_DAMAGE);
			addStat(stats, "armor", doll, Attributes.ARMOR);
		}
		return stats;
	}

	private static void addStat(List<Component> out, String key, LivingEntity doll, Holder<Attribute> attribute) {
		double value = GlossaryBestiary.stat(doll, attribute);
		if (value >= 0)
			out.add(statLine(key, Component.literal(NUMBER.format(value))));
	}

	private static Component statLine(String key, Component value) {
		return Component.translatable("glossary.witchercraft.stat." + key).withColor(GlossaryLayout.TEXT_DIM)
				.append(Component.literal(" ")).append(value.copy().withColor(GlossaryLayout.TEXT));
	}

	/** The oils of every category the creature is tagged into. */
	private static List<ItemStack> weaknesses(GlossaryBestiary.Entry entry) {
		List<ItemStack> oils = new ArrayList<>();
		for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values())
			if (GlossaryBestiary.entries(c).contains(entry))
				oils.add(c.oil());
		return oils;
	}

	// ---- Tutorials ---------------------------------------------------------------

	/** One row of the topic list: a group header or a topic. */
	private record TopicRow(GlossaryTutorials.Group group, GlossaryTutorials.Topic topic, int y, int h) {
	}

	private static List<TopicRow> topicRows(int y) {
		List<TopicRow> rows = new ArrayList<>();
		int ry = y + GlossaryLayout.TOPIC_Y + 4;
		for (GlossaryTutorials.Group group : GlossaryTutorials.GROUPS) {
			rows.add(new TopicRow(group, null, ry, GlossaryLayout.TOPIC_GROUP_H));
			ry += GlossaryLayout.TOPIC_GROUP_H;
			for (GlossaryTutorials.Topic topic : group.topics()) {
				rows.add(new TopicRow(group, topic, ry, GlossaryLayout.TOPIC_ROW_H));
				ry += GlossaryLayout.TOPIC_ROW_H;
			}
		}
		return rows;
	}

	private static GlossaryTutorials.Topic selectedTopic() {
		List<GlossaryTutorials.Topic> topics = GlossaryTutorials.topics();
		for (GlossaryTutorials.Topic t : topics)
			if (t.id().equals(topicId))
				return t;
		if (topics.isEmpty())
			return null;
		topicId = topics.get(0).id();
		return topics.get(0);
	}

	private void renderTutorials(GuiGraphicsExtractor g, Font font, int x, int y, int mouseX, int mouseY) {
		GlossaryTutorials.Topic selected = selectedTopic();

		// topic list
		int lx = x + GlossaryLayout.TOPIC_X, lw = GlossaryLayout.TOPIC_W;
		panel(g, lx, y + GlossaryLayout.TOPIC_Y, lw, GlossaryLayout.TOPIC_H);
		for (TopicRow row : topicRows(y)) {
			if (row.topic() == null) {
				g.text(font, row.group().title(), lx + GlossaryLayout.INFO_PAD, row.y() + 5, GlossaryLayout.TITLE, false);
				continue;
			}
			int rx = lx + 3, rw = lw - 6;
			boolean isSelected = row.topic() == selected;
			boolean hover = MapPage.inside(mouseX, mouseY, rx, row.y(), rw, row.h());
			if (isSelected) {
				g.fill(rx, row.y(), rx + rw, row.y() + row.h(), GlossaryLayout.ROW_SELECTED);
				g.fill(rx, row.y(), rx + 2, row.y() + row.h(), GlossaryLayout.ACCENT);
			} else if (hover) {
				g.fill(rx, row.y(), rx + rw, row.y() + row.h(), GlossaryLayout.ROW_HOVER);
			}
			g.text(font, Component.literal(fit(font, row.topic().title().getString(), rw - 20)), rx + 12, row.y() + (row.h() - 8) / 2,
					isSelected ? GlossaryLayout.SUBTAB_TEXT_ACTIVE : GlossaryLayout.TEXT, false);
		}
		if (selected == null)
			return;

		// illustration: the topic's own image, or its icon drawn large
		int px = x + GlossaryLayout.IMAGE_X, py = y + GlossaryLayout.IMAGE_Y, pw = GlossaryLayout.IMAGE_W, ph = GlossaryLayout.IMAGE_H;
		panel(g, px, py, pw, ph);
		Identifier image = selected.image();
		if (imageExists.computeIfAbsent(image, id -> Minecraft.getInstance().getResourceManager().getResource(id).isPresent())) {
			blit(g, image, px + 1, py + 1, pw - 2, ph - 2);
		} else if (!selected.itemIcon().isEmpty()) {
			drawScaledItem(g, selected.itemIcon(), px + pw / 2, py + ph / 2, GlossaryLayout.IMAGE_ICON_SCALE);
		} else if (selected.texture() != null) {
			float fit = 16f * GlossaryLayout.IMAGE_ICON_SCALE / Math.max(selected.textureW(), selected.textureH());
			int tw = Math.round(selected.textureW() * fit), th = Math.round(selected.textureH() * fit);
			blit(g, Identifier.parse(selected.texture()), px + (pw - tw) / 2, py + (ph - th) / 2, tw, th);
		}

		// text, in the info panel
		int ix = x + GlossaryLayout.INFO_X, iy = y + GlossaryLayout.INFO_Y, iw = GlossaryLayout.INFO_W;
		panel(g, ix, iy, iw, GlossaryLayout.INFO_H);
		drawTitle(g, font, selected.title(), ix, iy, iw);
		List<FormattedCharSequence> lines = new ArrayList<>(font.split(selected.text(), iw - 2 * GlossaryLayout.INFO_PAD));
		drawPagedText(g, font, x, y, lines, GlossaryLayout.BODY_Y, mouseX, mouseY);
	}

	// ---- shared: paged text in the info panel ------------------------------------

	private static int linesPerPage(int bodyY) {
		return Math.max(1, (GlossaryLayout.INFO_H - bodyY - GlossaryLayout.FOOTER_H) / GlossaryLayout.LINE_H);
	}

	private void drawPagedText(GuiGraphicsExtractor g, Font font, int x, int y, List<FormattedCharSequence> lines, int bodyY, int mouseX, int mouseY) {
		int perPage = linesPerPage(bodyY);
		int pages = Math.max(1, (lines.size() + perPage - 1) / perPage);
		textPage = clamp(textPage, pages);
		lastTextPages = pages;
		int tx = x + GlossaryLayout.INFO_X + GlossaryLayout.INFO_PAD, ty = y + GlossaryLayout.INFO_Y + bodyY;
		int first = textPage * perPage;
		for (int i = first; i < lines.size() && i < first + perPage; i++)
			g.text(font, lines.get(i), tx, ty + (i - first) * GlossaryLayout.LINE_H, GlossaryLayout.BODY, false);
		if (pages > 1)
			drawPager(g, font, textPager(font, x, y, pages), mouseX, mouseY);
	}

	private Pager textPager(Font font, int x, int y, int pages) {
		int centre = x + GlossaryLayout.INFO_X + GlossaryLayout.INFO_W / 2;
		int fy = y + GlossaryLayout.INFO_Y + GlossaryLayout.INFO_H - GlossaryLayout.FOOTER_H;
		return pager(font, centre - pagerWidth(font, pages) / 2, fy, textPage, pages);
	}

	// ---- shared drawing helpers ----------------------------------------------------

	private static Component pagerLabel(int page, int pages) {
		return Component.literal((page + 1) + " / " + pages);
	}

	private static int pagerWidth(Font font, int pages) {
		// sized for the widest label so the buttons do not jump between pages
		return 2 * GlossaryLayout.PAGE_BUTTON + 12 + font.width(pagerLabel(pages - 1, pages));
	}

	private static Pager pager(Font font, int left, int footerY, int page, int pages) {
		int b = GlossaryLayout.PAGE_BUTTON;
		int width = pagerWidth(font, pages);
		int y = footerY + (GlossaryLayout.FOOTER_H - b) / 2;
		Component label = pagerLabel(page, pages);
		int labelX = left + (width - font.width(label)) / 2;
		return new Pager(left, left + width - b, y, labelX, label, page, pages);
	}

	private static void drawPager(GuiGraphicsExtractor g, Font font, Pager p, int mouseX, int mouseY) {
		drawPageButton(g, font, p.prevX(), p.y(), "<", p.page() > 0, mouseX, mouseY);
		drawPageButton(g, font, p.nextX(), p.y(), ">", p.page() < p.pages() - 1, mouseX, mouseY);
		g.text(font, p.label(), p.labelX(), p.y() + (GlossaryLayout.PAGE_BUTTON - 8) / 2 + 1, GlossaryLayout.TEXT_DIM, false);
	}

	private static void drawPageButton(GuiGraphicsExtractor g, Font font, int bx, int by, String glyph, boolean enabled, int mouseX, int mouseY) {
		int b = GlossaryLayout.PAGE_BUTTON;
		boolean hover = enabled && MapPage.inside(mouseX, mouseY, bx, by, b, b);
		g.fill(bx, by, bx + b, by + b, hover ? GlossaryLayout.BUTTON_HOVER : GlossaryLayout.BUTTON_BG);
		MapPage.drawBorder(g, bx, by, b, b, hover ? GlossaryLayout.ACCENT : GlossaryLayout.CELL_BORDER);
		g.text(font, glyph, bx + (b - font.width(glyph)) / 2 + 1, by + (b - 8) / 2 + 1, enabled ? GlossaryLayout.TEXT : GlossaryLayout.TEXT_DIM, false);
	}

	private static void drawTitle(GuiGraphicsExtractor g, Font font, Component title, int ix, int iy, int iw) {
		String shown = fit(font, title.getString(), iw - 2 * GlossaryLayout.INFO_PAD);
		g.text(font, Component.literal(shown), ix + GlossaryLayout.INFO_PAD, iy + 6, GlossaryLayout.TITLE, false);
		divider(g, ix, iy + 17, iw);
	}

	private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		g.fill(x, y, x + w, y + h, GlossaryLayout.PANEL_BG);
		MapPage.drawBorder(g, x, y, w, h, GlossaryLayout.PANEL_BORDER);
	}

	private static void divider(GuiGraphicsExtractor g, int x, int y, int w) {
		g.fill(x + 4, y, x + w - 4, y + 1, GlossaryLayout.DIVIDER);
	}

	/** A skills-tree style cell: 1px frame around a dark inner square. */
	private static void cell(GuiGraphicsExtractor g, int x, int y, int size, int border) {
		g.fill(x, y, x + size, y + size, border);
		g.fill(x + 1, y + 1, x + size - 1, y + size - 1, GlossaryLayout.CELL_INNER);
	}

	private static void drawScaledItem(GuiGraphicsExtractor g, ItemStack stack, int centreX, int centreY, int scale) {
		g.pose().pushMatrix();
		g.pose().translate(centreX - 8 * scale, centreY - 8 * scale);
		g.pose().scale(scale, scale);
		g.fakeItem(stack, 0, 0);
		g.pose().popMatrix();
	}

	private static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int w, int h) {
		g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, w, h, w, h);
	}

	/** The text cut to fit a width, with "..." when cut. */
	private static String fit(Font font, String text, int width) {
		if (font.width(text) <= width)
			return text;
		return font.plainSubstrByWidth(text, width - font.width("...")) + "...";
	}

	private static int clamp(int page, int pages) {
		return Math.max(0, Math.min(page, pages - 1));
	}

	// ==========================================================================
	// state
	// ==========================================================================

	/** The selected category; on first open, the first one holding a creature from this mod. */
	private static GlossaryBestiary.Category currentCategory() {
		if (category != null)
			return category;
		GlossaryBestiary.Category firstFilled = null;
		for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values()) {
			List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(c);
			for (GlossaryBestiary.Entry e : entries)
				if (WitchercraftMod.MODID.equals(e.id().getNamespace()))
					return category = c;
			if (firstFilled == null && !entries.isEmpty())
				firstFilled = c;
		}
		return category = firstFilled != null ? firstFilled : GlossaryBestiary.Category.values()[0];
	}

	/** The selected creature, falling back to the category's first one. */
	private static GlossaryBestiary.Entry selectedEntry(List<GlossaryBestiary.Entry> entries) {
		for (GlossaryBestiary.Entry e : entries)
			if (e.type() == selectedType)
				return e;
		if (entries.isEmpty())
			return null;
		selectedType = entries.get(0).type();
		textPage = 0;
		return entries.get(0);
	}

	private static void selectCategory(GlossaryBestiary.Category c) {
		if (c == category)
			return;
		category = c;
		listPage = 0;
		List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(c);
		selectedType = entries.isEmpty() ? null : entries.get(0).type();
		textPage = 0;
	}

	// ==========================================================================
	// input
	// ==========================================================================

	@Override
	public boolean mouseClicked(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (button != 0)
			return false;
		Font font = Minecraft.getInstance().font;
		for (SubTab tab : SubTab.values()) {
			if (MapPage.inside(mouseX, mouseY, subTabX(x, w, tab.ordinal()), y + GlossaryLayout.SUBTAB_Y, GlossaryLayout.SUBTAB_W, GlossaryLayout.SUBTAB_H)) {
				if (tab != subTab) {
					subTab = tab;
					textPage = 0;
				}
				return true;
			}
		}
		return subTab == SubTab.BESTIARY ? clickBestiary(font, x, y, mouseX, mouseY) : clickTutorials(font, x, y, mouseX, mouseY);
	}

	private boolean clickBestiary(Font font, int x, int y, double mouseX, double mouseY) {
		GlossaryBestiary.Category cat = currentCategory();
		List<GlossaryBestiary.Entry> entries = GlossaryBestiary.entries(cat);

		// category rail
		for (GlossaryBestiary.Category c : GlossaryBestiary.Category.values()) {
			if (MapPage.inside(mouseX, mouseY, railCellX(x), railCellY(y, c.ordinal()), GlossaryLayout.RAIL_CELL, GlossaryLayout.RAIL_CELL)) {
				selectCategory(c);
				return true;
			}
		}

		// entry rows and the list's pager
		int lx = x + GlossaryLayout.LIST_X;
		int first = listPage * GlossaryLayout.LIST_ROWS;
		for (int i = first; i < entries.size() && i < first + GlossaryLayout.LIST_ROWS; i++) {
			if (MapPage.inside(mouseX, mouseY, lx + 3, listRowY(y, i - first), GlossaryLayout.LIST_W - 6, GlossaryLayout.LIST_ROW_H - 2)) {
				if (entries.get(i).type() != selectedType) {
					selectedType = entries.get(i).type();
					textPage = 0;
				}
				return true;
			}
		}
		int listPages = listPages(entries.size());
		if (listPages > 1) {
			int turn = pagerClick(listPager(font, x, y, listPages), mouseX, mouseY);
			if (turn != 0) {
				listPage = clamp(listPage + turn, listPages);
				return true;
			}
		}

		// doll: start a drag-to-rotate
		if (MapPage.inside(mouseX, mouseY, x + GlossaryLayout.DOLL_X, y + GlossaryLayout.DOLL_Y, GlossaryLayout.DOLL_W, GlossaryLayout.DOLL_H)) {
			draggingDoll = true;
			return true;
		}

		return clickTextPager(font, x, y, mouseX, mouseY) || insidePanels(x, y, mouseX, mouseY);
	}

	private boolean clickTutorials(Font font, int x, int y, double mouseX, double mouseY) {
		int rx = x + GlossaryLayout.TOPIC_X + 3, rw = GlossaryLayout.TOPIC_W - 6;
		for (TopicRow row : topicRows(y)) {
			if (row.topic() != null && MapPage.inside(mouseX, mouseY, rx, row.y(), rw, row.h())) {
				if (!row.topic().id().equals(topicId)) {
					topicId = row.topic().id();
					textPage = 0;
				}
				return true;
			}
		}
		return clickTextPager(font, x, y, mouseX, mouseY) || insidePanels(x, y, mouseX, mouseY);
	}

	private boolean clickTextPager(Font font, int x, int y, double mouseX, double mouseY) {
		if (lastTextPages <= 1)
			return false;
		int turn = pagerClick(textPager(font, x, y, lastTextPages), mouseX, mouseY);
		if (turn == 0)
			return false;
		textPage = clamp(textPage + turn, lastTextPages);
		return true;
	}

	/** -1 for the prev button, +1 for next, 0 for neither. */
	private static int pagerClick(Pager p, double mouseX, double mouseY) {
		int b = GlossaryLayout.PAGE_BUTTON;
		if (MapPage.inside(mouseX, mouseY, p.prevX(), p.y(), b, b))
			return -1;
		if (MapPage.inside(mouseX, mouseY, p.nextX(), p.y(), b, b))
			return 1;
		return 0;
	}

	/** Clicks on a panel's empty space are swallowed, like the other tabs do. */
	private static boolean insidePanels(int x, int y, double mouseX, double mouseY) {
		return MapPage.inside(mouseX, mouseY, x + GlossaryLayout.RAIL_X, y + GlossaryLayout.RAIL_Y, GlossaryLayout.INFO_X + GlossaryLayout.INFO_W - GlossaryLayout.RAIL_X,
				GlossaryLayout.INFO_H);
	}

	@Override
	public boolean mouseDragged(int x, int y, int w, int h, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!draggingDoll || button != 0)
			return false;
		// dragging right turns the creature's front to the right
		dollYaw = (float) ((dollYaw - dragX * DOLL_DRAG_DEGREES_PER_PX) % 360.0);
		return true;
	}

	@Override
	public boolean mouseReleased(int x, int y, int w, int h, double mouseX, double mouseY, int button) {
		if (!draggingDoll || button != 0)
			return false;
		draggingDoll = false;
		return true;
	}

	@Override
	public boolean mouseScrolled(int x, int y, int w, int h, double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY == 0)
			return false;
		int turn = scrollY < 0 ? 1 : -1;
		if (MapPage.inside(mouseX, mouseY, x + GlossaryLayout.INFO_X, y + GlossaryLayout.INFO_Y, GlossaryLayout.INFO_W, GlossaryLayout.INFO_H)) {
			textPage = clamp(textPage + turn, lastTextPages);
			return true;
		}
		if (subTab == SubTab.BESTIARY) {
			if (MapPage.inside(mouseX, mouseY, x + GlossaryLayout.LIST_X, y + GlossaryLayout.LIST_Y, GlossaryLayout.LIST_W, GlossaryLayout.LIST_H)) {
				listPage = clamp(listPage + turn, listPages(GlossaryBestiary.entries(currentCategory()).size()));
				return true;
			}
			if (MapPage.inside(mouseX, mouseY, x + GlossaryLayout.RAIL_X, y + GlossaryLayout.RAIL_Y, GlossaryLayout.RAIL_W, GlossaryLayout.RAIL_H)) {
				GlossaryBestiary.Category[] all = GlossaryBestiary.Category.values();
				selectCategory(all[Math.floorMod(currentCategory().ordinal() + turn, all.length)]);
				return true;
			}
		}
		return false;
	}
}
